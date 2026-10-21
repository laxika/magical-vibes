import { describe, expect, it } from 'vitest';
import type { CueEffect, CueEffectContext, CueRects } from '../../../services/cue-effect-player.service';
import { VisualCue, VisualCueType } from '../../../services/websocket.service';
import {
  COUNTERS_EFFECT, DAMAGE_EFFECT, DEFAULT_CUE_EFFECTS, DEFAULT_EFFECT_MAX_MS, IMPACT_MS, LEAVE_EFFECT, LIFE_EFFECT,
  PROJECTILE_MS, SPELL_CAST_EFFECT, SPELL_COUNTERED_EFFECT, SPELL_RESOLVED_EFFECT, counterElement, counterLabel
} from './default-effects';
import { sourcePalette } from './cue-palette';

const ALL_TYPES: VisualCueType[] = [
  'DESTROYED', 'SACRIFICED', 'EXILED', 'RETURNED_TO_HAND', 'DIED', 'DAMAGE_TO_PERMANENT', 'DAMAGE_TO_PLAYER',
  'SPELL_CAST', 'SPELL_RESOLVED', 'SPELL_COUNTERED', 'LIFE_GAINED', 'LIFE_LOST', 'COUNTERS_ADDED'
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

function rects(source: DOMRect | null, target: DOMRect | null): CueRects {
  return { source, target };
}

/** A 2D context that records where things are drawn and ignores everything else. */
function recordingCanvas() {
  const arcs: { x: number; y: number }[] = [];
  const texts: string[] = [];
  const state: Record<string | symbol, unknown> = {};
  const draw = new Proxy(state, {
    get(target, prop) {
      if (prop in target) return target[prop];
      if (prop === 'arc') return (x: number, y: number) => arcs.push({ x, y });
      if (prop === 'fillText' || prop === 'strokeText') return (text: string) => texts.push(text);
      if (prop === 'createRadialGradient') return () => ({ addColorStop: () => {} });
      return () => {};
    },
    set(target, prop, value) {
      target[prop] = value;
      return true;
    }
  }) as unknown as CanvasRenderingContext2D;
  return { draw, arcs, texts };
}

/** Runs each animation's frames at a few points of its progress and records the durations asked for. */
function fakeContext(draw: CanvasRenderingContext2D) {
  const durations: number[] = [];
  const context: CueEffectContext = {
    signal: new AbortController().signal,
    speed: 1,
    shake: () => {},
    animate: (durationMs, frame) => {
      durations.push(durationMs);
      [0, 0.1, 0.3, 0.5, 0.7, 0.9, 1].forEach(progress => frame(draw, progress));
      return Promise.resolve();
    }
  };
  return { context, durations };
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
    expect(effectFor(cue('SPELL_CAST'), r)).toBe(SPELL_CAST_EFFECT);
    expect(effectFor(cue('SPELL_RESOLVED'), r)).toBe(SPELL_RESOLVED_EFFECT);
    expect(effectFor(cue('SPELL_COUNTERED'), r)).toBe(SPELL_COUNTERED_EFFECT);
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

  it('draw a spell cue with nothing on screen in the middle of the window', async () => {
    const { draw, arcs } = recordingCanvas();
    const c = cue('SPELL_RESOLVED', { targetPermanentId: null });
    await SPELL_RESOLVED_EFFECT.play(c, rects(null, null), fakeContext(draw).context);

    expect(arcs).toContainEqual({ x: window.innerWidth / 2, y: window.innerHeight / 2 });
  });

  it('stop drawing when the target goes away mid-effect', async () => {
    const { draw, arcs } = recordingCanvas();
    const live = { source: null, target: TARGET as DOMRect | null };
    const context: CueEffectContext = {
      signal: new AbortController().signal, speed: 1, shake: () => {},
      animate: (_ms, frame) => {
        live.target = null;
        frame(draw, 0.5);
        return Promise.resolve();
      }
    };
    for (const c of [cue('DESTROYED'), cue('SPELL_CAST'), cue('DAMAGE_TO_PERMANENT')]) {
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
