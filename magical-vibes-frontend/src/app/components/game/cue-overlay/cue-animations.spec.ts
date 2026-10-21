import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  CUE_ANIMATIONS, CueAnimationData, cueAnimationsReady, drawCueAnimation, preloadCueAnimations, recolorAnimation,
  resetCueAnimations
} from './cue-animations';
import { PALETTES } from './cue-palette';

const AT = { x: 100, y: 100, unit: 50 };

function animation(): CueAnimationData & { layers: { shapes: unknown[] }[] } {
  return {
    w: 200, h: 200, ip: 0, op: 30,
    layers: [{ shapes: [{ ty: 'gr', it: [
      { ty: 'el' },
      { ty: 'fl', nm: '@accent', c: { a: 0, k: [0, 0, 0, 1] } },
      { ty: 'st', nm: '@c2', c: { a: 0, k: [0, 0, 0, 1] } },
      { ty: 'fl', nm: 'fixed', c: { a: 0, k: [1, 1, 1, 1] } }
    ] }] }]
  };
}

function drawingContext() {
  return { drawImage: vi.fn() } as unknown as CanvasRenderingContext2D & { drawImage: ReturnType<typeof vi.fn> };
}

afterEach(() => resetCueAnimations());

describe('Cue animations', () => {
  it('recolor palette slots to the look\'s colors and leave fixed colors and the original alone', () => {
    const original = animation();

    const fire = recolorAnimation(original, PALETTES.fire);

    const [accent, dark, fixed] = (fire.layers[0].shapes[0] as { it: { c: { k: number[] } }[] }).it.slice(1);
    expect(accent.c.k).toEqual([1, 196 / 255, 80 / 255, 1]);
    expect(dark.c.k).toEqual([232 / 255, 64 / 255, 24 / 255, 1]);
    expect(fixed.c.k).toEqual([1, 1, 1, 1]);
    expect(original).toEqual(animation());
  });

  it('draw nothing until they are loaded, so effects use their canvas fallback', () => {
    const draw = drawingContext();

    expect(cueAnimationsReady()).toBe(false);
    expect(drawCueAnimation(draw, 'impact', PALETTES.fire, 0.5, AT)).toBe(false);
    expect(draw.drawImage).not.toHaveBeenCalled();
  });

  it('load every animation, and stay on the fallback when one fails, trying again next time', async () => {
    const failing = vi.fn((name: string) => name === 'skull' ? Promise.reject(new Error('404')) : Promise.resolve(animation()));

    expect(await preloadCueAnimations(failing)).toBe(false);
    expect(failing.mock.calls.map(([name]) => name)).toEqual([...CUE_ANIMATIONS]);
    expect(cueAnimationsReady()).toBe(false);

    await preloadCueAnimations(failing);
    expect(failing).toHaveBeenCalledTimes(2 * CUE_ANIMATIONS.length);
  });

  it('load only once while a load is under way or has finished', async () => {
    const load = vi.fn(() => new Promise<CueAnimationData>(() => {}));

    void preloadCueAnimations(load);
    void preloadCueAnimations(load);

    expect(load).toHaveBeenCalledTimes(CUE_ANIMATIONS.length);
  });
});
