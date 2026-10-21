import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CUE_DEBUG_STORAGE_KEY, CUE_EFFECTS, CueEffect, CueEffectPlayer, CueSurface } from './cue-effect-player.service';
import { DEBUG_FLASH_EFFECT } from '../components/game/cue-overlay/debug-flash.effect';
import { DEFAULT_CUE_EFFECTS } from '../components/game/cue-overlay/default-effects';
import { CueElementRegistry } from './cue-elements.service';
import { VisualCue } from './websocket.service';

function cue(overrides: Partial<VisualCue>): VisualCue {
  return {
    type: 'DAMAGE_TO_PERMANENT', sourceCardId: null, sourceName: null, sourceColors: [],
    targetPermanentId: null, targetPlayerId: null, targetCardId: null, amount: 3, counterType: null,
    ...overrides
  };
}

/** An element at a fixed spot; jsdom lays nothing out, so rectangles are stubbed. */
function element(x: number, y: number, size = 10): HTMLElement {
  const el = document.createElement('div');
  document.body.appendChild(el);
  el.getBoundingClientRect = () => new DOMRect(x, y, size, size);
  return el;
}

function surface(): CueSurface & { animate: ReturnType<typeof vi.fn> } {
  return { animate: vi.fn(() => Promise.resolve()) };
}

function setUp(effects: CueEffect[]) {
  TestBed.configureTestingModule({ providers: [{ provide: CUE_EFFECTS, useValue: effects }] });
  return { player: TestBed.inject(CueEffectPlayer), elements: TestBed.inject(CueElementRegistry) };
}

const ANY_TARGET: CueEffect = {
  matches: (_cue, rects) => rects.target !== null,
  play: (_cue, _rects, { animate }) => animate(400, () => {})
};

describe('CueEffectPlayer', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
    document.body.innerHTML = '';
  });

  it('registers the default effects, so a cue whose target is on screen plays by default', () => {
    const player = TestBed.inject(CueEffectPlayer);
    player.attach(surface());
    TestBed.inject(CueElementRegistry).register('permanent', 'bear', element(0, 0));

    expect(TestBed.inject(CUE_EFFECTS)).toEqual(DEFAULT_CUE_EFFECTS);
    expect(player.canPlay(cue({ targetPermanentId: 'bear' }))).toBe(true);
    expect(player.canPlay(cue({ targetPermanentId: 'gone' }))).toBe(false);
  });

  it('puts the debug flash in front of the defaults when the debug flag is set', () => {
    localStorage.setItem(CUE_DEBUG_STORAGE_KEY, '1');
    try {
      expect(TestBed.inject(CUE_EFFECTS)).toEqual([DEBUG_FLASH_EFFECT, ...DEFAULT_CUE_EFFECTS]);
    } finally {
      localStorage.removeItem(CUE_DEBUG_STORAGE_KEY);
    }
  });

  it('plays nothing without an overlay attached, or once it detaches', () => {
    const { player, elements } = setUp([ANY_TARGET]);
    elements.register('permanent', 'bear', element(0, 0));
    const bolt = cue({ targetPermanentId: 'bear' });

    expect(player.canPlay(bolt)).toBe(false);
    const detach = player.attach(surface());
    expect(player.canPlay(bolt)).toBe(true);
    detach();
    expect(player.canPlay(bolt)).toBe(false);
  });

  it('picks the first matching effect and scales its duration by speed', async () => {
    const skipped: CueEffect = { matches: () => false, play: vi.fn(() => Promise.resolve()) };
    const { player, elements } = setUp([skipped, ANY_TARGET]);
    const canvas = surface();
    player.attach(canvas);
    elements.register('permanent', 'bear', element(0, 0));
    const signal = new AbortController().signal;

    await player.play(cue({ targetPermanentId: 'bear' }), { signal, speed: 2 });

    expect(skipped.play).not.toHaveBeenCalled();
    expect(canvas.animate).toHaveBeenCalledWith(200, expect.any(Function), signal);
  });

  it('can not play a cue whose target is not on screen', () => {
    const { player } = setUp([ANY_TARGET]);
    player.attach(surface());

    expect(player.canPlay(cue({ targetPermanentId: 'gone' }))).toBe(false);
  });

  it('gives effects rectangles that follow the element as it moves', () => {
    const { player, elements } = setUp([]);
    const bear = element(10, 20);
    elements.register('permanent', 'bear', bear);
    const rects = player.rects(cue({ targetPermanentId: 'bear' }));

    expect(rects.target?.x).toBe(10);
    bear.getBoundingClientRect = () => new DOMRect(50, 60, 10, 10);
    expect(rects.target?.x).toBe(50);
  });
});

describe('CueElementRegistry', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
    document.body.innerHTML = '';
  });

  it('finds a target permanent, else a target player', () => {
    const elements = TestBed.inject(CueElementRegistry);
    elements.register('permanent', 'bear', element(1, 0));
    elements.register('player', 'two', element(2, 0));

    expect(elements.targetRect(cue({ targetPermanentId: 'bear', targetPlayerId: 'two' }))?.x).toBe(1);
    expect(elements.targetRect(cue({ targetPlayerId: 'two' }))?.x).toBe(2);
    expect(elements.targetRect(cue({}))).toBeNull();
  });

  it('finds a countered or targeted spell on the stack by its card id', () => {
    const elements = TestBed.inject(CueElementRegistry);
    elements.register('stack', 'spell', element(3, 0));

    expect(elements.targetRect(cue({ type: 'SPELL_COUNTERED', targetCardId: 'spell' }))?.x).toBe(3);
    expect(elements.targetRect(cue({ type: 'SPELL_COUNTERED', targetCardId: 'gone' }))).toBeNull();
  });

  it('finds a source card on the battlefield before the stack', () => {
    const elements = TestBed.inject(CueElementRegistry);
    elements.register('stack', 'sorcerer-card', element(1, 0));
    elements.register('permanent', 'sorcerer', element(2, 0), 'sorcerer-card');
    elements.register('stack', 'bolt-card', element(3, 0));

    expect(elements.sourceRect(cue({ sourceCardId: 'sorcerer-card' }))?.x).toBe(2);
    expect(elements.sourceRect(cue({ sourceCardId: 'bolt-card' }))?.x).toBe(3);
    expect(elements.sourceRect(cue({ sourceCardId: 'resolved' }))).toBeNull();
    expect(elements.sourceRect(cue({}))).toBeNull();
  });

  it('skips elements that are detached or not laid out, and forgets unregistered ones', () => {
    const elements = TestBed.inject(CueElementRegistry);
    const hidden = element(1, 0, 0);
    const detached = element(2, 0);
    detached.remove();
    const shown = element(3, 0);
    elements.register('stack', 'bolt', hidden);
    elements.register('stack', 'bolt', detached);
    const unregister = elements.register('stack', 'bolt', shown);

    expect(elements.rect('stack', 'bolt')?.x).toBe(3);
    unregister();
    expect(elements.rect('stack', 'bolt')).toBeNull();
  });
});
