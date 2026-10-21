import type { CueEffect } from '../../../services/cue-effect-player.service';

/** How long the debug flash lasts at normal speed. */
export const DEBUG_FLASH_MS = 400;

/**
 * Debug effect: a fading yellow flash over the cue's target, to check that effects land on the
 * right element in every layout. Registered only when the debug flag is set.
 */
export const DEBUG_FLASH_EFFECT: CueEffect = {
  matches: (_cue, rects) => rects.target !== null,
  play: (_cue, rects, { animate }) => animate(DEBUG_FLASH_MS, (draw, progress) => {
    const target = rects.target;
    if (!target) return;
    const alpha = 1 - progress;
    draw.fillStyle = `rgba(255, 230, 80, ${0.6 * alpha})`;
    draw.fillRect(target.x, target.y, target.width, target.height);
    draw.lineWidth = 3;
    draw.strokeStyle = `rgba(255, 255, 255, ${alpha})`;
    draw.strokeRect(target.x, target.y, target.width, target.height);
  })
};
