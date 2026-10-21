import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { CueAnchorDirective } from './cue-anchor.directive';
import {
  CUE_HOLD_ANIMATION_ID, animateCueElement, burnThresholds, createCardBurn, releaseCueHolds, shardShapes
} from './cue-card-motion';
import { seededRandom } from './cue-particles';

/** A stand-in for a Web Animations `Animation` that the test finishes or aborts by hand. */
function fakeAnimation(id = '') {
  let settle: () => void = () => {};
  const finished = new Promise<void>(resolve => settle = resolve);
  return { id, finished, finish: vi.fn(() => settle()), cancel: vi.fn(() => settle()) };
}

function animatedElement() {
  const element = document.createElement('div');
  document.body.appendChild(element);
  let animation = fakeAnimation();
  const animate = vi.fn((_keyframes: Keyframe[], options: KeyframeAnimationOptions) => {
    animation = fakeAnimation(options.id);
    return animation;
  });
  element.animate = animate as unknown as HTMLElement['animate'];
  return { element, animate, animation: () => animation };
}

describe('Card animations', () => {
  afterEach(() => {
    vi.useRealTimers();
    document.body.innerHTML = '';
  });

  it('play once and are cancelled when playback stops', async () => {
    const { element, animate, animation } = animatedElement();
    const abort = new AbortController();

    const done = animateCueElement(element, [{ opacity: 0 }], { durationMs: 300, easing: 'ease-in' }, abort.signal, 1000);
    abort.abort();
    await done;

    expect(animate).toHaveBeenCalledWith([{ opacity: 0 }], { duration: 300, easing: 'ease-in', fill: 'none', id: '' });
    expect(animation().cancel).toHaveBeenCalled();
    expect(animation().finish).not.toHaveBeenCalled();
  });

  it('hold a leaving card\'s last frame, and jump to it when playback stops early', async () => {
    const { element, animate, animation } = animatedElement();
    const abort = new AbortController();

    const done = animateCueElement(element, [{ opacity: 0 }], { durationMs: 300, hold: true }, abort.signal, 1000);
    abort.abort();
    await done;

    expect(animate).toHaveBeenCalledWith([{ opacity: 0 }], expect.objectContaining({ fill: 'forwards', id: CUE_HOLD_ANIMATION_ID }));
    expect(animation().finish).toHaveBeenCalled();
    expect(animation().cancel).not.toHaveBeenCalled();
  });

  it('let go of a held frame when the element is still on the page long after', () => {
    vi.useFakeTimers();
    const kept = animatedElement();
    animateCueElement(kept.element, [], { durationMs: 300, hold: true }, new AbortController().signal, 1000);
    const removed = animatedElement();
    animateCueElement(removed.element, [], { durationMs: 300, hold: true }, new AbortController().signal, 1000);
    removed.element.remove();

    vi.advanceTimersByTime(1299);
    expect(kept.animation().cancel).not.toHaveBeenCalled();
    vi.advanceTimersByTime(1);
    expect(kept.animation().cancel).toHaveBeenCalled();
    expect(removed.animation().cancel).not.toHaveBeenCalled();
  });

  it('do nothing once playback has stopped, or where the Web Animations API is missing', async () => {
    const { element, animate } = animatedElement();
    const stopped = new AbortController();
    stopped.abort();

    await animateCueElement(element, [], { durationMs: 300 }, stopped.signal, 1000);
    await animateCueElement(document.createElement('div'), [], { durationMs: 300 }, new AbortController().signal, 1000);

    expect(animate).not.toHaveBeenCalled();
  });

  it('let go of held frames only', () => {
    const element = document.createElement('div');
    const held = fakeAnimation(CUE_HOLD_ANIMATION_ID);
    const other = fakeAnimation('tap');
    element.getAnimations = () => [held, other] as unknown as Animation[];

    releaseCueHolds(element);

    expect(held.cancel).toHaveBeenCalled();
    expect(other.cancel).not.toHaveBeenCalled();
  });
});

@Component({
  standalone: true,
  imports: [CueAnchorDirective],
  template: '<div appCueAnchor="stack" [cueAnchorId]="id()"></div>'
})
class StackEntryHost {
  readonly id = signal<string | null>('first');
}

describe('Cue anchors', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('let go of held frames when the element starts showing something else, and only then', () => {
    const fixture = TestBed.createComponent(StackEntryHost);
    fixture.detectChanges();
    const element = fixture.nativeElement.querySelector('div') as HTMLElement;
    const held = fakeAnimation(CUE_HOLD_ANIMATION_ID);
    element.getAnimations = () => [held] as unknown as Animation[];

    fixture.componentInstance.id.set('first');
    fixture.detectChanges();
    expect(held.cancel).not.toHaveBeenCalled();

    fixture.componentInstance.id.set('second');
    fixture.detectChanges();
    expect(held.cancel).toHaveBeenCalledOnce();

    fixture.destroy();
    expect(held.cancel).toHaveBeenCalledOnce();
  });
});

describe('Card burn', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('leaves the card whole at the start and burns all of it by the end, rim before char before card', () => {
    const [gone, rim, card] = burnThresholds(0);
    expect(gone).toBeLessThan(rim);
    expect(rim).toBeLessThan(card);
    // The noise field over a card runs from about 0.35 at its edges to about 1.05 in its middle.
    expect(card).toBeLessThan(0.35);
    expect(burnThresholds(1)[0]).toBeGreaterThan(1.05);
    expect(burnThresholds(2)).toEqual(burnThresholds(1));
  });

  it('is a filter on the page until it is removed', () => {
    const burn = createCardBurn(7, { rim: 'rgb(255, 150, 40)', ash: [0, 0, 0] });
    const id = burn.filter.match(/^url\(#(.+)\)$/)![1];

    expect(document.getElementById(id)?.tagName).toBe('filter');
    expect(document.querySelector(`#${id} feTurbulence`)?.getAttribute('seed')).toBe('7');
    expect(document.querySelector(`#${id} feFlood`)?.getAttribute('flood-color')).toBe('rgb(255, 150, 40)');
    burn.remove();
    expect(document.getElementById(id)).toBeNull();
  });
});

/** The area of a polygon (shoelace formula). */
function area(points: readonly (readonly [number, number])[]): number {
  let twice = 0;
  points.forEach(([x, y], i) => {
    const [nx, ny] = points[(i + 1) % points.length];
    twice += x * ny - nx * y;
  });
  return Math.abs(twice) / 2;
}

describe('Shatter', () => {
  it('cuts the element into shards that cover it exactly, inside its box', () => {
    for (const seed of [1, 2, 3, 42]) {
      const shards = shardShapes(seededRandom(seed), 9);

      expect(shards).toHaveLength(9);
      expect(shards.reduce((sum, shard) => sum + area(shard.points), 0)).toBeCloseTo(100 * 100, 6);
      for (const { points } of shards) {
        for (const [x, y] of points) {
          expect(x).toBeGreaterThanOrEqual(0);
          expect(x).toBeLessThanOrEqual(100);
          expect(y).toBeGreaterThanOrEqual(0);
          expect(y).toBeLessThanOrEqual(100);
        }
      }
    }
  });
});
