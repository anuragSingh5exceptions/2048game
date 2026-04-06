(function () {
  var boardShell;
  var timers = {};

  function getBoardShell() {
    if (!boardShell) {
      boardShell = document.querySelector(".board-shell");
    }
    return boardShell;
  }

  function vibrateFallback(kind) {
    if (!navigator.vibrate) {
      return;
    }

    var patterns = {
      button: 16,
      move: 10,
      merge: [14, 24, 26],
      undo: [18, 18, 18],
      win: [24, 36, 28, 40, 54],
      lose: [52, 28, 24]
    };

    navigator.vibrate(patterns[kind] || 12);
  }

  function haptic(kind) {
    try {
      if (window.Android && typeof window.Android.performHaptic === "function") {
        window.Android.performHaptic(kind);
        return;
      }
    } catch (error) {
      // Fall back to navigator.vibrate when the native bridge is unavailable.
    }

    vibrateFallback(kind);
  }

  function pulseBoard(name, duration) {
    var element = getBoardShell();

    if (!element) {
      return;
    }

    var className = "board-" + name;
    clearTimeout(timers[className]);
    element.classList.remove(className);
    void element.offsetWidth;
    element.classList.add(className);
    timers[className] = window.setTimeout(function () {
      element.classList.remove(className);
    }, duration || 520);
  }

  window.GameFeedback = {
    haptic: haptic,
    pulseBoard: pulseBoard
  };
})();
