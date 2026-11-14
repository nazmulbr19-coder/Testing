(function () {
  var params = new URLSearchParams(window.location.search);
  var isTv = params.get('tv') === '1';

  if (isTv) {
    document.documentElement.classList.add('tv-mode');
  }

  function updateAdaptiveBlockWidth() {
    var htmlEl = document.documentElement;
    var vw = htmlEl.clientWidth || window.innerWidth;
    var vh = htmlEl.clientHeight || window.innerHeight;
    var cols = 10;
    var rows = 20;

    var mainMenu = document.getElementById('mainMenu');
    var inMenu = mainMenu && !mainMenu.classList.contains('hide');
    document.body.classList.toggle('mobile-playing', !isTv && !inMenu);

    // Reserve vertical space for title + touch pad (on phone) or footer (on TV)
    var touchEl = document.getElementById('touchControlContainer');
    var touchShowing = !isTv && !inMenu && touchEl && window.getComputedStyle(touchEl).display !== 'none';
    var reservedH = isTv ? 105 : (touchShowing ? 225 : 130);
    var availH = Math.max(220, vh - reservedH);
    var availW = Math.max(180, vw - (isTv ? 420 : 190));

    var byHeight = Math.floor(availH / rows);
    var byWidth = Math.floor(availW / cols);
    var minBlock = 10;
    var maxBlock = isTv ? 28 : 24;
    var computed = Math.max(minBlock, Math.min(maxBlock, byHeight, byWidth));

    if (computed > 0) {
      htmlEl.style.setProperty('--block-width', computed + 'px');
    }
  }

  function applyTvControlsLegend() {
    if (!isTv) return;
    var box = document.querySelector('.instructionBox');
    if (!box) return;
    box.innerHTML =
      '<h3>TV Remote</h3>' +
      '<div class="instructionBoxText"><p class="textRight">Move</p><p class="textleft">◀ ▼ ▶</p></div>' +
      '<div class="instructionBoxText"><p class="textRight">Rotate</p><p class="textleft">▲ / ⏪</p></div>' +
      '<div class="instructionBoxText"><p class="textRight">Smash</p><p class="textleft">OK</p></div>' +
      '<div class="instructionBoxText"><p class="textRight">Save</p><p class="textleft">⏩</p></div>' +
      '<div class="instructionBoxText"><p class="textRight">Pause</p><p class="textleft">Back</p></div>';
  }

  // Track which UI element has TV D-Pad focus when not actively falling
  var focusIndex = 0;
  var currentFocusList = [];

  function isElementVisible(el) {
    if (!el) return false;
    var style = window.getComputedStyle(el);
    return style.display !== 'none' && style.visibility !== 'hidden' && !el.closest('.hide');
  }

  function isMenuVisible() {
    var menu = document.getElementById('mainMenu');
    return isElementVisible(menu);
  }

  function isActivelyPlaying() {
    if (isMenuVisible()) return false;
    var startBtn = document.getElementById('startButton');
    var endGame = document.getElementById('endGame');
    var pausedEl = document.getElementById('gamePaused');
    var endVisible = endGame && window.getComputedStyle(endGame).display !== 'none';
    var pauseVisible = pausedEl && window.getComputedStyle(pausedEl).display !== 'none';
    if (!startBtn || endVisible || pauseVisible) return false;
    return startBtn.textContent.trim().toLowerCase() === 'pause';
  }

  function getFocusableTargets() {
    if (isMenuVisible()) {
      return [
        document.getElementById('enduranceMode'),
        document.getElementById('rushMode'),
        document.getElementById('sprintMode'),
        document.getElementById('musicBoxContainer')
      ].filter(Boolean);
    }
    return [
      document.getElementById('startButton'),
      document.getElementById('resetButton'),
      document.getElementById('backMenu'),
      document.getElementById('musicBoxContainer')
    ].filter(Boolean);
  }

  function clearFocusHighlight() {
    var prev = document.querySelectorAll('.tv-focused');
    for (var i = 0; i < prev.length; i++) {
      prev[i].classList.remove('tv-focused');
    }
  }

  function syncFocusHighlight() {
    clearFocusHighlight();
    if (!isTv || isActivelyPlaying()) return;
    currentFocusList = getFocusableTargets();
    if (currentFocusList.length === 0) return;
    if (focusIndex < 0 || focusIndex >= currentFocusList.length) {
      focusIndex = 0;
    }
    currentFocusList[focusIndex].classList.add('tv-focused');
  }

  function moveFocus(delta) {
    currentFocusList = getFocusableTargets();
    if (currentFocusList.length === 0) return;
    focusIndex = (focusIndex + delta + currentFocusList.length) % currentFocusList.length;
    syncFocusHighlight();
  }

  function clickFocusedTarget() {
    currentFocusList = getFocusableTargets();
    if (currentFocusList.length === 0) return;
    var target = currentFocusList[focusIndex] || currentFocusList[0];
    if (target.id === 'musicBoxContainer') {
      var sw = document.getElementById('musicSwitch');
      if (sw) sw.click();
    } else {
      target.click();
    }
    focusIndex = 0;
    setTimeout(function () {
      updateAdaptiveBlockWidth();
      syncFocusHighlight();
    }, 30);
  }

  function dispatchGameKey(keyCode, keyName, codeName) {
    var evt = new KeyboardEvent('keydown', {
      keyCode: keyCode,
      which: keyCode,
      key: keyName,
      code: codeName,
      bubbles: true,
      cancelable: true
    });
    document.dispatchEvent(evt);
  }

  // Called from Android TetrisWebView.dispatchKeyEvent
  window.__abidexTetrisKey = function (action, repeatCount) {
    var repeat = repeatCount || 0;

    if (isActivelyPlaying()) {
      switch (action) {
        case 'left':
          dispatchGameKey(37, 'ArrowLeft', 'ArrowLeft');
          return 'handled';
        case 'right':
          dispatchGameKey(39, 'ArrowRight', 'ArrowRight');
          return 'handled';
        case 'down':
          dispatchGameKey(40, 'ArrowDown', 'ArrowDown');
          return 'handled';
        case 'up':
          if (repeat === 0) dispatchGameKey(90, 'z', 'KeyZ');
          return 'handled';
        case 'ok':
          if (repeat === 0) dispatchGameKey(38, 'ArrowUp', 'ArrowUp');
          return 'handled';
        case 'rotLeft':
          if (repeat === 0) dispatchGameKey(65, 'a', 'KeyA');
          return 'handled';
        case 'save':
          if (repeat === 0) dispatchGameKey(69, 'e', 'KeyE');
          return 'handled';
        case 'pause':
          if (repeat === 0) {
            var startBtn = document.getElementById('startButton');
            if (startBtn) startBtn.click();
            focusIndex = 0;
            setTimeout(syncFocusHighlight, 30);
          }
          return 'handled';
      }
      return 'ignored';
    }

    // Menu, Paused, Not Started, or End Game state: navigate buttons via D-Pad
    if (repeat > 0 && action === 'ok') return 'handled';
    switch (action) {
      case 'up':
      case 'left':
        moveFocus(-1);
        return 'handled';
      case 'down':
      case 'right':
        moveFocus(1);
        return 'handled';
      case 'ok':
        if (repeat === 0) clickFocusedTarget();
        return 'handled';
      case 'pause':
        if (repeat === 0 && !isMenuVisible()) {
          var s = document.getElementById('startButton');
          if (s) s.click();
          setTimeout(syncFocusHighlight, 30);
        }
        return 'handled';
    }
    return 'ignored';
  };

  window.__abidexTetrisBack = function () {
    if (isMenuVisible()) {
      return 'exit';
    }
    if (isActivelyPlaying()) {
      var startBtn = document.getElementById('startButton');
      if (startBtn) startBtn.click();
      focusIndex = 0;
      setTimeout(syncFocusHighlight, 30);
      return 'handled';
    }
    var backBtn = document.getElementById('backMenu');
    if (backBtn) {
      backBtn.click();
      focusIndex = 0;
      setTimeout(function () {
        updateAdaptiveBlockWidth();
        syncFocusHighlight();
      }, 30);
      return 'handled';
    }
    return 'exit';
  };

  window.__abidexTetrisPauseAudio = function () {
    if (Array.isArray(window.__abidexAudios)) {
      window.__abidexAudios.forEach(function (a) {
        try {
          a.pause();
        } catch (e) {}
      });
    }
    if (isActivelyPlaying()) {
      var startBtn = document.getElementById('startButton');
      if (startBtn) startBtn.click();
      focusIndex = 0;
      setTimeout(syncFocusHighlight, 30);
    }
  };

  window.addEventListener('DOMContentLoaded', function () {
    applyTvControlsLegend();
    updateAdaptiveBlockWidth();
    syncFocusHighlight();

    // Observe game overlays / panel switches to keep focus & sizing accurate
    var obs = new MutationObserver(function () {
      updateAdaptiveBlockWidth();
      syncFocusHighlight();
    });
    var targets = ['mainMenu', 'playgroundContainer', 'endGame', 'gamePaused', 'startButton'];
    targets.forEach(function (id) {
      var el = document.getElementById(id);
      if (el) {
        obs.observe(el, { attributes: true, childList: true, characterData: true, subtree: true });
      }
    });
  });

  window.addEventListener('resize', updateAdaptiveBlockWidth);
})();
