import { describe, expect, it } from 'vitest';
import { RenderedTextBox, largestFittingSize, renderedTextKey } from './card-text-fit';

const PRECISION = 0.1;
const MIN = 7;
const MAX = 11;

/**
 * A measurement stand-in. `heightAt` is the model of how tall the content renders at a given
 * size; anything at or under `capacity` fits. Records every size it was asked about so tests
 * can assert on the search itself, and exposes the size left applied at the end — the one the
 * card actually renders at, which is what clipping is really about.
 */
function box(capacity: number, heightAt: (size: number) => number) {
  const probed: number[] = [];
  let applied: number | null = null;
  return {
    probed,
    get applied() { return applied; },
    get appliedFits() { return applied !== null && heightAt(applied) <= capacity; },
    applyAndTest: (size: number) => {
      probed.push(size);
      applied = size;
      return heightAt(size) <= capacity;
    },
  };
}

/** Smooth model: height proportional to size, as text would be if it never rewrapped. */
const linear = (perPx: number) => (size: number) => size * perPx;

/**
 * Wrapping model: the real one. Text occupies whole lines, the line count jumps as the size
 * crosses a wrap boundary, and height is lines x size x leading. This is what defeats a fixed
 * walk — between two adjacent walk steps the height can jump by a whole line.
 */
const wrapping = (boundaries: number[], leading = 1.3) => (size: number) => {
  const lines = 1 + boundaries.filter(b => size > b).length;
  return lines * size * leading;
};

describe('largestFittingSize', () => {

  it('returns the maximum when everything fits, without searching', () => {
    const b = box(1000, linear(10));
    expect(largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest)).toBe(MAX);
    expect(b.probed).toEqual([MAX]);
    expect(b.applied).toBe(MAX);
  });

  it('returns null when even the minimum overflows, and leaves the minimum applied', () => {
    const b = box(1, linear(10));
    expect(largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest)).toBeNull();
    // The caller decides what gives — dropping flavour text, say — so the floor has to be
    // what is on the element when it finds out.
    expect(b.applied).toBe(MIN);
  });

  it('finds the largest fitting size to within the precision', () => {
    // Fits up to exactly 9.37px.
    const b = box(93.7, linear(10));
    const size = largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest)!;
    expect(size).toBeLessThanOrEqual(9.37);
    expect(size).toBeGreaterThan(9.37 - PRECISION * 2);
    expect(b.appliedFits).toBe(true);
  });

  it('settles on the precision grid rather than a bisection remainder', () => {
    const size = largestFittingSize(MIN, MAX, PRECISION, box(93.7, linear(10)).applyAndTest)!;
    expect(Math.round(size * 10)).toBeCloseTo(size * 10, 6);
  });

  it('crosses a wrap cliff that a fixed half-pixel walk falls short of', () => {
    // Group Project: two lines up to 7.15px, four beyond it (both sentences rewrap at once).
    // Capacity holds three lines at 7.1 but not five, so 7.1 is the answer and 7.5 is not.
    const heightAt = wrapping([7.15, 7.15, 9]);
    const capacity = heightAt(7.1) + 0.5;
    const b = box(capacity, heightAt);

    const size = largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest)!;

    expect(size).toBeGreaterThanOrEqual(7.1);
    expect(size).toBeLessThan(7.2);
    expect(b.appliedFits).toBe(true);
  });

  it('leaves a fitting size applied for every capacity, which is the no-clipping guarantee', () => {
    const heightAt = wrapping([7.4, 8.1, 8.1, 9.6, 10.2]);
    for (let capacity = 5; capacity <= 160; capacity += 0.25) {
      const b = box(capacity, heightAt);
      const size = largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest);
      if (size === null) {
        expect(heightAt(MIN)).toBeGreaterThan(capacity);
      } else {
        expect(b.appliedFits, `capacity ${capacity} settled on ${size}`).toBe(true);
      }
    }
  });

  it('never settles on a size a whole precision step below one that fits', () => {
    const heightAt = wrapping([7.4, 8.1, 9.6]);
    for (let capacity = 5; capacity <= 160; capacity += 0.25) {
      const size = largestFittingSize(MIN, MAX, PRECISION, box(capacity, heightAt).applyAndTest);
      if (size !== null && size < MAX) {
        // Whatever it settled on, a size two precision steps larger must genuinely overflow —
        // otherwise the search left readable size on the table, which is the other failure
        // mode and the one that reads as "why is this text so small".
        expect(heightAt(size + PRECISION * 2)).toBeGreaterThan(capacity);
      }
    }
  });

  it('keeps a non-monotonic measurement from producing clipped text', () => {
    // Bisection assumes taller-with-size. If that is ever violated the search may land badly,
    // but it must still leave behind a size that fit when applied.
    const heightAt = (size: number) => (size > 9 && size < 9.5 ? 0 : size * 10);
    for (let capacity = 5; capacity <= 120; capacity += 0.5) {
      const b = box(capacity, heightAt);
      const size = largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest);
      if (size !== null) {
        expect(b.appliedFits, `capacity ${capacity} settled on ${size}`).toBe(true);
      }
    }
  });

  it('converges in far fewer probes than walking the range would take', () => {
    const b = box(93.7, linear(10));
    largestFittingSize(MIN, MAX, PRECISION, b.applyAndTest);
    // Walking 7 to 11 at 0.1 would be 40 probes; bisection is logarithmic.
    expect(b.probed.length).toBeLessThan(12);
  });

  it('handles a degenerate range where the floor is the ceiling', () => {
    expect(largestFittingSize(MIN, MIN, PRECISION, box(1000, linear(1)).applyAndTest)).toBe(MIN);
    expect(largestFittingSize(MIN, MIN, PRECISION, box(1, linear(10)).applyAndTest)).toBeNull();
  });
});

describe('renderedTextKey', () => {

  const box: RenderedTextBox = {
    text: 'Flashback Raising spirits is easier than taming them.',
    imageCount: 3,
  };

  it('is stable for unchanged content, so a settled fit is not redone every check', () => {
    // fitTextToBox runs from ngAfterViewChecked, i.e. on every change detection pass; the key is
    // the only thing keeping it from re-measuring every card on the board continuously.
    expect(renderedTextKey(box)).toBe(renderedTextKey({ ...box }));
  });

  it('changes when the text in the box changes', () => {
    expect(renderedTextKey({ ...box, text: 'Destroy target creature.' })).not.toBe(renderedTextKey(box));
  });

  it('changes when flavour text appears in the box', () => {
    /* The regression, and the reason this key is taken from the DOM rather than the card model.
       Flavour arrives from a set-wide fetch, so the model has it a whole change detection pass
       before the box does. A model-derived key called those two states identical: a fit landing in
       the gap measured a box with no flavour in it, stored a key already claiming flavour was
       present, and the render that followed then matched that key and skipped the fit. Every
       affected card in the running app showed flavour at exactly the box font size — the signature
       of a flavour element the fitter never touched — and overflowed by up to 56px. */
    const withoutFlavor = renderedTextKey({ text: 'Destroy target creature.', imageCount: 0 });
    const withFlavor = renderedTextKey({
      text: 'Destroy target creature.Some flavour worth reading.',
      imageCount: 0,
    });
    expect(withFlavor).not.toBe(withoutFlavor);
  });

  it('changes when a mana symbol turns from text into an image', () => {
    // `{W}` is literal text until its symbol loads and then becomes an inline image. Both halves
    // of that show up here — text lost, image gained — so no separate version counter is needed.
    const asText = renderedTextKey({ text: 'Flashback {4}{W}{W}', imageCount: 0 });
    const asImages = renderedTextKey({ text: 'Flashback ', imageCount: 3 });
    expect(asImages).not.toBe(asText);
  });

  it('changes when only the image count changes', () => {
    // Two symbols and three symbols wrap differently even with identical surrounding text.
    expect(renderedTextKey({ ...box, imageCount: 2 })).not.toBe(renderedTextKey(box));
  });

  it('does not let a field boundary shift disguise a change', () => {
    // Naive concatenation of count and text would collide these two.
    expect(renderedTextKey({ text: '1abc', imageCount: 2 }))
        .not.toBe(renderedTextKey({ text: 'abc', imageCount: 21 }));
  });
});
