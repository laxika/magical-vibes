import { describe, expect, it } from 'vitest';
import type { CueEffect, CueEffectContext, CueRects } from '../../../services/cue-effect-player.service';
import { MAX_CUE_PLAYBACK_MS } from '../../../services/cue-playback.service';
import { VisualCue, VisualCueType } from '../../../services/websocket.service';
import { DEFAULT_CUE_EFFECTS } from './default-effects';
import { DOOM_BLADE_MS, LIGHTNING_BOLT_MS, SHOWPIECE_EFFECTS } from './showpiece-effects';

const TARGET = new DOMRect(400, 300, 100, 140);
const TARGET_MIDDLE = { x: 450, y: 370 };

function cue(type: VisualCueType, sourceName: string | null, overrides: Partial<VisualCue> = {}): VisualCue {
  return {
    type, sourceCardId: 'source', sourceName, sourceColors: ['RED'], targetPermanentId: 'bear', targetPlayerId: null,
    targetCardId: null, amount: 3, counterType: null, ...overrides
  };
}

const bolt = (type: VisualCueType = 'DAMAGE_TO_PERMANENT') => cue(type, 'Lightning Bolt');
const doomBlade = (type: VisualCueType = 'DESTROYED') => cue(type, 'Doom Blade', { sourceColors: ['BLACK'] });

function rects(target: DOMRect | null, targetElement: HTMLElement | null = null): CueRects {
  return { source: null, target, targetElement };
}

/** A 2D context that records path points, translations and text, and ignores everything else. */
function recordingCanvas() {
  const points: { x: number; y: number; progress: number }[] = [];
  const translations: { x: number; y: number; progress: number }[] = [];
  const texts: string[] = [];
  const recorder = { progress: 0 };
  const draw = new Proxy({} as Record<string | symbol, unknown>, {
    get(target, prop) {
      if (prop in target) return target[prop];
      if (prop === 'moveTo' || prop === 'lineTo') return (x: number, y: number) => points.push({ x, y, progress: recorder.progress });
      if (prop === 'translate') return (x: number, y: number) => translations.push({ x, y, progress: recorder.progress });
      if (prop === 'fillText' || prop === 'strokeText') return (text: string) => texts.push(text);
      if (prop === 'createRadialGradient') return () => ({ addColorStop: () => {} });
      return () => {};
    },
    set(target, prop, value) {
      target[prop] = value;
      return true;
    }
  }) as unknown as CanvasRenderingContext2D;
  return { draw, recorder, points, translations, texts };
}

const PROGRESS = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1];

/**
 * Runs each animation at a range of progress values, recording the durations asked for, when it
 * shook, and the element animations started (with the progress they started at).
 */
function fakeContext(canvas = recordingCanvas()) {
  const durations: number[] = [];
  const shakes: number[] = [];
  const elementAnimations: { keyframes: Keyframe[]; durationMs: number; hold: boolean; progress: number }[] = [];
  const context: CueEffectContext = {
    signal: new AbortController().signal,
    speed: 1,
    shake: () => shakes.push(canvas.recorder.progress),
    animateElement: (_element, keyframes, options) => {
      elementAnimations.push({ keyframes, durationMs: options.durationMs, hold: !!options.hold, progress: canvas.recorder.progress });
      return Promise.resolve();
    },
    animate: (durationMs, frame) => {
      durations.push(durationMs);
      for (const progress of PROGRESS) {
        canvas.recorder.progress = progress;
        frame(canvas.draw, progress);
      }
      return Promise.resolve();
    }
  };
  return { context, durations, shakes, elementAnimations, ...canvas };
}

const EFFECTS = [...SHOWPIECE_EFFECTS, ...DEFAULT_CUE_EFFECTS];

function effectFor(c: VisualCue, target: DOMRect | null = TARGET): CueEffect | undefined {
  return EFFECTS.find(effect => effect.matches(c, rects(target)));
}

/** How long the effect `CUE_EFFECTS` would pick for `c` plays. */
async function durationOf(c: VisualCue): Promise<number> {
  const { context, durations } = fakeContext();
  await effectFor(c)!.play(c, rects(TARGET), context);
  return durations.reduce((sum, ms) => sum + ms, 0);
}

describe('Showpiece effects', () => {
  it('play for Lightning Bolt\'s damage and Doom Blade\'s destroy, in front of the defaults', async () => {
    expect(await durationOf(bolt())).toBe(LIGHTNING_BOLT_MS);
    expect(await durationOf(bolt('DAMAGE_TO_PLAYER'))).toBe(LIGHTNING_BOLT_MS);
    expect(await durationOf(doomBlade())).toBe(DOOM_BLADE_MS);
  });

  it('leave other cards, the cards\' other cues and face-down sources to the defaults', () => {
    const defaults = [
      cue('DAMAGE_TO_PERMANENT', 'Shock'), cue('DESTROYED', 'Murder'), cue('DAMAGE_TO_PERMANENT', null), cue('DESTROYED', null)
    ];
    for (const c of defaults) {
      expect(DEFAULT_CUE_EFFECTS, `${c.sourceName} ${c.type}`).toContain(effectFor(c));
    }
  });

  it('play nothing while Lightning Bolt or Doom Blade is cast or resolves', () => {
    for (const c of [bolt('SPELL_CAST'), bolt('SPELL_RESOLVED'), doomBlade('SPELL_CAST'), doomBlade('SPELL_RESOLVED')]) {
      expect(effectFor(c), `${c.sourceName} ${c.type}`).toBeUndefined();
    }
  });

  it('need their target on screen', () => {
    expect(effectFor(bolt(), null)).toBeUndefined();
    expect(effectFor(doomBlade(), null)).toBeUndefined();
  });

  it('stay inside the playback budget', async () => {
    expect(LIGHTNING_BOLT_MS).toBeLessThan(MAX_CUE_PLAYBACK_MS);
    expect(DOOM_BLADE_MS).toBeLessThan(MAX_CUE_PLAYBACK_MS);
  });
});

describe('Lightning Bolt', () => {
  it('strikes down from the top of the window and reaches the target', async () => {
    const { context, points } = fakeContext();

    await effectFor(bolt())!.play(bolt(), rects(TARGET), context);

    const starts = points.filter(p => p.progress === 0);
    expect(Math.min(...starts.map(p => p.y))).toBeLessThan(0);
    expect(Math.max(...starts.map(p => p.y))).toBeLessThan(TARGET.top);
    expect(points.filter(p => p.progress === 0.2)).toContainEqual({ ...TARGET_MIDDLE, progress: 0.2 });
  });

  it('shakes the view once, when it strikes', async () => {
    const { context, shakes } = fakeContext();

    await effectFor(bolt())!.play(bolt(), rects(TARGET), context);

    expect(shakes).toEqual([0.2]);
  });

  it('shakes the target with a red flash from when it strikes to the end of the effect', async () => {
    const { context, elementAnimations } = fakeContext();

    await effectFor(bolt())!.play(bolt(), rects(TARGET, document.createElement('div')), context);

    expect(elementAnimations).toHaveLength(1);
    const [{ keyframes, durationMs, hold, progress }] = elementAnimations;
    expect(progress).toBe(0.2);
    expect(hold).toBe(false);
    expect(durationMs).toBeCloseTo(LIGHTNING_BOLT_MS * 0.8);
    expect(keyframes.some(keyframe => String(keyframe['filter']).includes('sepia'))).toBe(true);
  });

  it('shows the damage dealt and never the card\'s name', async () => {
    const { context, texts } = fakeContext();

    await effectFor(bolt())!.play(bolt(), rects(TARGET), context);

    expect(texts).toContain('-3');
    expect(texts.join()).not.toContain('Lightning Bolt');
  });

  it('flickers: the bolt\'s path changes while it shows, and is the same for the same cue', async () => {
    const first = fakeContext();
    const again = fakeContext();

    await effectFor(bolt())!.play(bolt(), rects(TARGET), first.context);
    await effectFor(bolt())!.play(bolt(), rects(TARGET), again.context);

    const at = (progress: number) => first.points.filter(p => p.progress === progress).map(({ x, y }) => ({ x, y }));
    expect(at(0.2)).not.toEqual(at(0.3));
    expect(again.points).toEqual(first.points);
  });
});

describe('Doom Blade', () => {
  it('forms a skull above the target, drops it onto the target, and lets it go', async () => {
    const { context, translations } = fakeContext();

    await effectFor(doomBlade())!.play(doomBlade(), rects(TARGET), context);

    const skullAt = (progress: number) => translations.find(t => t.progress === progress);
    expect(skullAt(0.1)).toBeUndefined();
    expect(skullAt(0.3)!.y).toBeLessThan(TARGET.top);
    expect(skullAt(0.3)!.x).toBe(TARGET_MIDDLE.x);
    expect(skullAt(0.7)).toEqual({ ...TARGET_MIDDLE, progress: 0.7 });
    expect(skullAt(0.8)).toBeUndefined();
  });

  it('keeps the cloud on screen when the target is at the top of the window', async () => {
    const { context, translations } = fakeContext();
    const top = new DOMRect(400, 0, 100, 140);

    await effectFor(doomBlade())!.play(doomBlade(), rects(top), context);

    expect(translations.find(t => t.progress === 0.3)!.y).toBeGreaterThan(0);
  });

  it('shakes the view once, when the skull lands', async () => {
    const { context, shakes } = fakeContext();

    await effectFor(doomBlade())!.play(doomBlade(), rects(TARGET), context);

    expect(shakes).toEqual([0.7]);
  });

  it('burns the target away in violet once the skull lands, and holds it gone', async () => {
    const { context, elementAnimations } = fakeContext();

    await effectFor(doomBlade())!.play(doomBlade(), rects(TARGET, document.createElement('div')), context);

    expect(elementAnimations).toHaveLength(1);
    const [{ keyframes, durationMs, hold, progress }] = elementAnimations;
    expect(progress).toBe(0.7);
    expect(hold).toBe(true);
    expect(durationMs).toBeCloseTo(DOOM_BLADE_MS * 0.3);
    expect(String(keyframes[0]['filter'])).toMatch(/^url\(#cue-burn-\d+\)$/);
    expect(keyframes[keyframes.length - 1]['opacity']).toBe(0);
    expect(document.querySelector('filter[id^="cue-burn-"]')).toBeNull();
  });
});

describe('Showpieces without a target', () => {
  it('stop drawing when the target goes away mid-effect', async () => {
    for (const c of [bolt(), doomBlade()]) {
      const canvas = recordingCanvas();
      const live = { source: null, target: TARGET as DOMRect | null, targetElement: null };
      const context: CueEffectContext = {
        signal: new AbortController().signal, speed: 1, shake: () => {}, animateElement: () => Promise.resolve(),
        animate: (_ms, frame) => {
          live.target = null;
          frame(canvas.draw, 0.5);
          return Promise.resolve();
        }
      };

      await effectFor(c)!.play(c, live, context);

      expect(canvas.points).toEqual([]);
      expect(canvas.translations).toEqual([]);
    }
  });
});
