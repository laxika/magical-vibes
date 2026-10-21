import { afterEach, describe, expect, it } from 'vitest';
import type { CueEffect, CueEffectContext, CueRects } from '../../../services/cue-effect-player.service';
import { VisualCue, VisualCueType } from '../../../services/websocket.service';
import {
  COUNTERS_EFFECT, DAMAGE_EFFECT, DEFAULT_CUE_EFFECTS, DEFAULT_EFFECT_MAX_MS, IMPACT_MS, LEAVE_EFFECT, LIFE_EFFECT,
  PROJECTILE_MS, SPELL_COUNTERED_EFFECT, counterElement, counterLabel
} from './default-effects';
import { sourcePalette } from './cue-palette';
import { CueElementAnimation, burnThresholds } from './cue-card-motion';

/** Every cue type with a default effect; casting and resolving a spell have none. */
const ALL_TYPES: VisualCueType[] = [
  'DESTROYED', 'SACRIFICED', 'EXILED', 'RETURNED_TO_HAND', 'DIED', 'DAMAGE_TO_PERMANENT', 'DAMAGE_TO_PLAYER',
  'SPELL_COUNTERED', 'LIFE_GAINED', 'LIFE_LOST', 'COUNTERS_ADDED'
];

function cue(type: VisualCueType, overrides: Partial<VisualCue> = {}): VisualCue {
  return {
    type, sourceCardId: 'source', sourceName: 'Secret Name', sourceColors: ['RED'], targetPermanentId: 'bear',
    targetPlayerId: null, targetCardId: null, amount: 3, counterType: type === 'COUNTERS_ADDED' ? 'PLUS_ONE_PLUS_ONE' : null,
    ...overrides
  };
}

const TARGET = new DOMRect(400, 300, 100, 140);
const SOURCE = new DOMRect(100, 600, 100, 140);

function rects(source: DOMRect | null, target: DOMRect | null, targetElement: HTMLElement | null = null): CueRects {
  return { source, target, targetElement };
}

/** A 2D context that records where things are drawn and ignores everything else. */
function recordingCanvas() {
  const arcs: { x: number; y: number }[] = [];
  const moves: { x: number; y: number }[] = [];
  const texts: string[] = [];
  const state: Record<string | symbol, unknown> = {};
  const draw = new Proxy(state, {
    get(target, prop) {
      if (prop in target) return target[prop];
      if (prop === 'arc') return (x: number, y: number) => arcs.push({ x, y });
      if (prop === 'moveTo') return (x: number, y: number) => moves.push({ x, y });
      if (prop === 'fillText' || prop === 'strokeText') return (text: string) => texts.push(text);
      if (prop === 'createRadialGradient') return () => ({ addColorStop: () => {} });
      return () => {};
    },
    set(target, prop, value) {
      target[prop] = value;
      return true;
    }
  }) as unknown as CanvasRenderingContext2D;
  return { draw, arcs, moves, texts };
}

interface ElementAnimation {
  element: HTMLElement;
  keyframes: Keyframe[];
  options: CueElementAnimation;
}

/**
 * Runs each animation's frames at a few points of its progress and records the durations asked
 * for, and the element animations started (which finish at once). `onFrame` runs after each frame.
 */
function fakeContext(draw: CanvasRenderingContext2D, onFrame: (progress: number) => void = () => {}) {
  const durations: number[] = [];
  const elementAnimations: ElementAnimation[] = [];
  const context: CueEffectContext = {
    signal: new AbortController().signal,
    speed: 1,
    shake: () => {},
    animate: (durationMs, frame) => {
      durations.push(durationMs);
      [0, 0.1, 0.3, 0.5, 0.7, 0.9, 1].forEach(progress => {
        frame(draw, progress);
        onFrame(progress);
      });
      return Promise.resolve();
    },
    animateElement: (element, keyframes, options) => {
      elementAnimations.push({ element, keyframes, options });
      return Promise.resolve();
    }
  };
  return { context, durations, elementAnimations };
}

function effectFor(c: VisualCue, r: CueRects): CueEffect | undefined {
  return DEFAULT_CUE_EFFECTS.find(effect => effect.matches(c, r));
}

describe('Default cue effects', () => {
  it('have an effect for every cue type whose target is on screen', () => {
    for (const type of ALL_TYPES) {
      expect(effectFor(cue(type), rects(SOURCE, TARGET)), type).toBeDefined();
    }
  });

  it('play spell cues even when nothing they point at is on screen, and skip the rest', () => {
    for (const type of ALL_TYPES) {
      const effect = effectFor(cue(type), rects(null, null));
      if (type.startsWith('SPELL_')) {
        expect(effect, type).toBeDefined();
      } else {
        expect(effect, type).toBeUndefined();
      }
    }
  });

  it('pick the effect for the cue\'s kind', () => {
    const r = rects(SOURCE, TARGET);
    expect(effectFor(cue('DAMAGE_TO_PLAYER'), r)).toBe(DAMAGE_EFFECT);
    expect(effectFor(cue('DIED'), r)).toBe(LEAVE_EFFECT);
    expect(effectFor(cue('LIFE_LOST'), r)).toBe(LIFE_EFFECT);
    expect(effectFor(cue('COUNTERS_ADDED'), r)).toBe(COUNTERS_EFFECT);
    expect(effectFor(cue('SPELL_COUNTERED'), r)).toBe(SPELL_COUNTERED_EFFECT);
  });

  it('play nothing for a spell being cast or resolving', () => {
    for (const r of [rects(SOURCE, TARGET), rects(null, null)]) {
      expect(effectFor(cue('SPELL_CAST'), r)).toBeUndefined();
      expect(effectFor(cue('SPELL_RESOLVED'), r)).toBeUndefined();
    }
  });

  it('stay within the time budget, projectile included', async () => {
    for (const type of ALL_TYPES) {
      const { draw } = recordingCanvas();
      const { context, durations } = fakeContext(draw);
      const c = cue(type);
      await effectFor(c, rects(SOURCE, TARGET))!.play(c, rects(SOURCE, TARGET), context);

      const total = durations.reduce((sum, d) => sum + d, 0);
      expect(total, type).toBeGreaterThan(0);
      expect(total, type).toBeLessThanOrEqual(DEFAULT_EFFECT_MAX_MS);
    }
  });

  it('move the card only alongside the drawing, so it stays within the budget too', async () => {
    for (const type of ALL_TYPES) {
      const { context, durations, elementAnimations } = fakeContext(recordingCanvas().draw);
      const c = cue(type);
      const target = cardElement();
      await effectFor(c, rects(SOURCE, TARGET, target))!.play(c, rects(SOURCE, TARGET, target), context);

      for (const { options } of elementAnimations) {
        expect(options.durationMs, type).toBeLessThanOrEqual(durations[durations.length - 1]);
      }
    }
  });

  it('fly a projectile from the source to the target for damage, and only hit when the source is gone', async () => {
    const withSource = fakeContext(recordingCanvas().draw);
    await DAMAGE_EFFECT.play(cue('DAMAGE_TO_PERMANENT'), rects(SOURCE, TARGET), withSource.context);
    expect(withSource.durations).toEqual([PROJECTILE_MS, IMPACT_MS]);

    const withoutSource = fakeContext(recordingCanvas().draw);
    await DAMAGE_EFFECT.play(cue('DAMAGE_TO_PERMANENT'), rects(null, TARGET), withoutSource.context);
    expect(withoutSource.durations).toEqual([IMPACT_MS]);

    const selfDamage = fakeContext(recordingCanvas().draw);
    await DAMAGE_EFFECT.play(cue('DAMAGE_TO_PERMANENT'), rects(TARGET, TARGET), selfDamage.context);
    expect(selfDamage.durations).toEqual([IMPACT_MS]);
  });

  it('draw the impact around the target and show the damage dealt', async () => {
    const { draw, arcs, texts } = recordingCanvas();
    await DAMAGE_EFFECT.play(cue('DAMAGE_TO_PERMANENT'), rects(null, TARGET), fakeContext(draw).context);

    expect(texts).toContain('-3');
    expect(arcs.length).toBeGreaterThan(10);
    const cx = TARGET.left + TARGET.width / 2;
    const cy = TARGET.top + TARGET.height / 2;
    for (const { x, y } of arcs) {
      expect(Math.hypot(x - cx, y - cy)).toBeLessThan(TARGET.width * 1.5);
    }
  });

  it('show life and counters as labels', async () => {
    const shown = async (c: VisualCue) => {
      const { draw, texts } = recordingCanvas();
      await effectFor(c, rects(null, TARGET))!.play(c, rects(null, TARGET), fakeContext(draw).context);
      return texts;
    };

    expect(await shown(cue('LIFE_GAINED', { targetPermanentId: null, targetPlayerId: 'me', amount: 4 }))).toContain('+4');
    expect(await shown(cue('LIFE_LOST', { targetPermanentId: null, targetPlayerId: 'me', amount: 2 }))).toContain('-2');
    expect(await shown(cue('COUNTERS_ADDED', { amount: 2 }))).toContain('+2/+2');
  });

  it('never draw the source\'s name', async () => {
    for (const type of ALL_TYPES) {
      const { draw, texts } = recordingCanvas();
      const c = cue(type);
      await effectFor(c, rects(SOURCE, TARGET))!.play(c, rects(SOURCE, TARGET), fakeContext(draw).context);
      expect(texts.join(' '), type).not.toContain('Secret');
    }
  });

  it('draw a countered spell with nothing on screen in the middle of the window', async () => {
    const { draw, moves } = recordingCanvas();
    const c = cue('SPELL_COUNTERED', { targetPermanentId: null });
    await SPELL_COUNTERED_EFFECT.play(c, rects(null, null), fakeContext(draw).context);

    expect(moves).toContainEqual({ x: window.innerWidth / 2, y: window.innerHeight / 2 });
  });

  it('stop drawing when the target goes away mid-effect', async () => {
    const { draw, arcs } = recordingCanvas();
    const live = { source: null, target: TARGET as DOMRect | null, targetElement: null };
    const context: CueEffectContext = {
      signal: new AbortController().signal, speed: 1, shake: () => {}, animateElement: () => Promise.resolve(),
      animate: (_ms, frame) => {
        live.target = null;
        frame(draw, 0.5);
        return Promise.resolve();
      }
    };
    for (const c of [cue('DESTROYED'), cue('SPELL_COUNTERED'), cue('DAMAGE_TO_PERMANENT')]) {
      live.target = TARGET;
      await effectFor(c, live)!.play(c, live, context);
    }

    expect(arcs).toEqual([]);
  });
});

describe('Effect looks', () => {
  it('follow a single source color, and are neutral for colorless, multicolor and hidden sources', () => {
    expect(sourcePalette(['RED']).element).toBe('fire');
    expect(sourcePalette(['BLUE']).element).toBe('water');
    expect(sourcePalette(['BLACK']).element).toBe('shadow');
    expect(sourcePalette(['WHITE']).element).toBe('light');
    expect(sourcePalette(['GREEN']).element).toBe('nature');
    expect(sourcePalette([]).element).toBe('arcane');
    expect(sourcePalette(['RED', 'GREEN']).element).toBe('arcane');
  });

  it('pick counters\' look by counter type before source color', () => {
    expect(counterElement('PLUS_ONE_PLUS_ONE', ['BLACK'])).toBe('nature');
    expect(counterElement('MINUS_ONE_MINUS_ONE', ['GREEN'])).toBe('shadow');
    expect(counterElement('LOYALTY', [])).toBe('gold');
    expect(counterElement('CHARGE', ['BLUE'])).toBe('water');
    expect(counterElement(null, [])).toBe('arcane');
  });

  it('label counters by what they do', () => {
    expect(counterLabel('PLUS_ONE_PLUS_ONE', 2)).toBe('+2/+2');
    expect(counterLabel('MINUS_ONE_MINUS_ONE', 1)).toBe('-1/-1');
    expect(counterLabel('PLUS_ONE_PLUS_ZERO', 3)).toBe('+3/+0');
    expect(counterLabel('MINUS_TWO_MINUS_ONE', 1)).toBe('-2/-1');
    expect(counterLabel('LOYALTY', 4)).toBe('+4 loyalty');
    expect(counterLabel('ARROWHEAD', 1)).toBe('+1 arrowhead');
    expect(counterLabel(null, 1)).toBe('+1');
  });
});

/** A card on the page; jsdom lays nothing out, so its rectangle is stubbed to {@link TARGET}. */
function cardElement(): HTMLElement {
  const parent = document.createElement('div');
  const card = document.createElement('div');
  card.className = 'card';
  card.textContent = 'Bear';
  card.getBoundingClientRect = () => TARGET;
  parent.appendChild(card);
  document.body.appendChild(parent);
  return card;
}

/** The `translate` of a keyframe, as numbers. */
function shift(keyframe: Keyframe): [number, number] {
  const [x, y] = String(keyframe['translate']).split(' ').map(parseFloat);
  return [x, y];
}

const LEAVE_TYPES: VisualCueType[] = ['DESTROYED', 'SACRIFICED', 'EXILED', 'RETURNED_TO_HAND', 'DIED'];

describe('Default effects on the card itself', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('knock a damaged card away from its source with a red flash, and let it settle', async () => {
    const card = cardElement();
    const { context, elementAnimations } = fakeContext(recordingCanvas().draw);

    await DAMAGE_EFFECT.play(cue('DAMAGE_TO_PERMANENT'), rects(SOURCE, TARGET, card), context);

    expect(elementAnimations).toHaveLength(1);
    const [{ element, keyframes, options }] = elementAnimations;
    expect(element).toBe(card);
    expect(options.hold).toBeFalsy();
    // The source is below and to the left, so the hit lands up and to the right.
    const [x, y] = shift(keyframes[1]);
    expect(x).toBeGreaterThan(0);
    expect(y).toBeLessThan(0);
    expect(keyframes.some(keyframe => String(keyframe['filter']).includes('sepia'))).toBe(true);
    expect(shift(keyframes[keyframes.length - 1])).toEqual([0, 0]);
    expect(keyframes[keyframes.length - 1]['filter']).toBe('none');
  });

  it('shake a damaged card side to side when its source is not on screen', async () => {
    const { context, elementAnimations } = fakeContext(recordingCanvas().draw);

    await DAMAGE_EFFECT.play(cue('DAMAGE_TO_PERMANENT'), rects(null, TARGET, cardElement()), context);

    const shifts = elementAnimations[0].keyframes.map(shift);
    expect(shifts.every(([, y]) => y === 0)).toBe(true);
    expect(shifts.some(([x]) => x > 0)).toBe(true);
    expect(shifts.some(([x]) => x < 0)).toBe(true);
  });

  it('take every leaving card off and hold it invisible until the new state removes it', async () => {
    for (const type of LEAVE_TYPES) {
      const card = cardElement();
      const { context, elementAnimations } = fakeContext(recordingCanvas().draw);

      await LEAVE_EFFECT.play(cue(type), rects(SOURCE, TARGET, card), context);

      expect(elementAnimations, type).toHaveLength(1);
      const [{ element, keyframes, options }] = elementAnimations;
      expect(element, type).toBe(card);
      expect(options.hold, type).toBe(true);
      expect(keyframes[0]['opacity'], type).toBe(1);
      expect(keyframes[keyframes.length - 1]['opacity'], type).toBe(0);
    }
  });

  it('burn destroyed, dead, sacrificed and exiled cards away, and take the burn off the page after', async () => {
    for (const type of ['DESTROYED', 'SACRIFICED', 'EXILED', 'DIED'] as VisualCueType[]) {
      const filters: (string | null)[] = [];
      const { context, elementAnimations } = fakeContext(recordingCanvas().draw,
        () => filters.push(document.querySelector('filter[id^="cue-burn-"]')?.id ?? null));

      await LEAVE_EFFECT.play(cue(type), rects(SOURCE, TARGET, cardElement()), context);

      const filter = String(elementAnimations[0].keyframes[0]['filter']);
      expect(filter, type).toMatch(/^url\(#cue-burn-\d+\)$/);
      expect(elementAnimations[0].keyframes.every(keyframe => keyframe['filter'] === filter), type).toBe(true);
      expect(filters.every(id => id !== null && filter === `url(#${id})`), type).toBe(true);
      expect(document.querySelector('filter[id^="cue-burn-"]'), type).toBeNull();
    }
  });

  it('burn the card along with the effect, from whole to gone', async () => {
    const intercepts: number[][] = [];
    const { context } = fakeContext(recordingCanvas().draw, () => intercepts.push(
      [...document.querySelectorAll('filter[id^="cue-burn-"] feFuncA')].map(fn => Number(fn.getAttribute('intercept')))));

    await LEAVE_EFFECT.play(cue('DESTROYED'), rects(SOURCE, TARGET, cardElement()), context);

    const expected = (burn: number) => burnThresholds(burn).map(threshold => -25 * threshold);
    expect(intercepts[0]).toEqual(expected(0));
    expect(intercepts[intercepts.length - 1]).toEqual(expected(1));
  });

  it('bleach an exiled card white as it burns out', async () => {
    const slopes: number[] = [];
    const { context } = fakeContext(recordingCanvas().draw,
      () => slopes.push(Number(document.querySelector('filter[id^="cue-burn-"] feFuncR')?.getAttribute('slope'))));

    await LEAVE_EFFECT.play(cue('EXILED'), rects(SOURCE, TARGET, cardElement()), context);

    expect(slopes[0]).toBe(1);
    expect(slopes[slopes.length - 1]).toBeCloseTo(0.1);
  });

  it('spin a bounced card down into the vortex without burning it', async () => {
    const { context, elementAnimations } = fakeContext(recordingCanvas().draw);

    await LEAVE_EFFECT.play(cue('RETURNED_TO_HAND'), rects(SOURCE, TARGET, cardElement()), context);

    const { keyframes } = elementAnimations[0];
    const last = keyframes[keyframes.length - 1];
    expect(keyframes.some(keyframe => 'filter' in keyframe)).toBe(false);
    expect(Number(last['scale'])).toBeLessThan(0.2);
    expect(parseFloat(String(last['rotate']))).toBeGreaterThan(180);
  });

  it('shatter a countered spell into shards laid over it, hide it meanwhile, and clear the shards after', async () => {
    const entry = cardElement();
    const { context, elementAnimations } = fakeContext(recordingCanvas().draw);

    await SPELL_COUNTERED_EFFECT.play(cue('SPELL_COUNTERED', { targetPermanentId: null, targetCardId: 'spell' }),
      rects(SOURCE, TARGET, entry), context);

    const hidden = elementAnimations.filter(animation => animation.element === entry);
    expect(hidden).toHaveLength(1);
    expect(hidden[0].options.hold).toBe(true);
    expect(hidden[0].keyframes.every(keyframe => keyframe['opacity'] === 0)).toBe(true);
    const shards = elementAnimations.filter(animation => animation.element !== entry);
    expect(shards.length).toBeGreaterThanOrEqual(6);
    for (const shard of shards) {
      expect(shard.element.textContent).toBe('Bear');
      expect(shard.element.style.clipPath).toMatch(/^polygon\(/);
      expect(shard.element.style.left).toBe(`${TARGET.left}px`);
      expect(shard.element.style.top).toBe(`${TARGET.top}px`);
      expect(shard.element.getAttribute('aria-hidden')).toBe('true');
      expect(shard.options.hold).toBeFalsy();
      expect(shard.keyframes[shard.keyframes.length - 1]['opacity']).toBe(0);
    }
    expect(entry.parentElement!.children).toHaveLength(1);
  });

  it('leave the card alone when only its rectangle is known', async () => {
    for (const type of ALL_TYPES) {
      const { context, elementAnimations } = fakeContext(recordingCanvas().draw);
      const c = cue(type);
      await effectFor(c, rects(SOURCE, TARGET))!.play(c, rects(SOURCE, TARGET), context);
      expect(elementAnimations, type).toEqual([]);
    }
  });
});
