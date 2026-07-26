/**
 * The size search and the staleness key behind the card frame's auto-fitting text.
 *
 * <p>Both live outside the component on purpose. Every bug this file exists to prevent came
 * from one of two places — a search that settled on a size the box could not actually hold, or
 * a cached fit that was never redone after the thing it measured changed underneath it — and
 * neither is observable from the rendered card until someone notices text quietly clipped or
 * quietly too small. Here the measurement is a parameter, so both are testable without a
 * layout engine.
 */

/**
 * What a text box currently lays out. Taken from the rendered DOM rather than from the card model,
 * which is the whole point.
 *
 * <p>Keying the cache off the model instead cost three rounds of misdiagnosis. Flavour text arrives
 * from a set-wide fetch, so the model carries it before any change detection pass renders it; a fit
 * landing in that window measures a box with no flavour in it and stores a key that already claims
 * flavour is present. The render that follows matches the stored key, the guard skips the fit, and
 * the flavour is never measured at any size — it just hangs off the bottom of the frame. Every
 * affected card in the running app showed flavour text at exactly the box font size, which is what
 * a flavour element the fitter never touched looks like.
 *
 * <p>A key derived from what is actually in the box cannot disagree with what gets measured. It
 * also subsumes what were separate inputs before: rules text, keywords, granted abilities and the
 * prepare-spell inset are all text in the box, so none needs a signal of its own.
 */
export interface RenderedTextBox {
  /** `textContent` of the box: every string it lays out, whatever produced it. */
  text: string;
  /**
   * Mana symbols it lays out, counted separately because none of them is text.
   *
   * <p>A symbol is a font glyph drawn by a `::before` rule, so it takes a symbol's width and
   * contributes nothing at all to `textContent` — which makes the count the only thing telling
   * `{T}: Add {G}.` apart from `{T}: Add {G}{G}.`. Both are the string ": Add ." once the
   * symbols are out of it, and they do not wrap the same. A key blind to this reports the two
   * cards as identical content, and the second one keeps whatever size was fitted for the first.
   */
  symbolCount: number;
}

/**
 * Key identifying the content a fit was computed against. A fit may be reused only while this is
 * unchanged; any difference means the box has to be measured again.
 */
export function renderedTextKey(box: RenderedTextBox): string {
  // JSON rather than joining on a separator: this is arbitrary card text and may contain whatever
  // separator gets picked, so a shift across the boundary would otherwise produce one key for two
  // different contents and suppress the re-fit that difference should force.
  return JSON.stringify([box.symbolCount, box.text]);
}

/**
 * Largest size in `[minSize, maxSize]` whose content fits, found by bisection to `precision`
 * and left applied by the final call to `applyAndTest`. Returns null when even `minSize`
 * overflows — the caller has to decide what gives — and in that case leaves `minSize` applied.
 *
 * <p>`applyAndTest` both sets the size and reports whether the result fits, so the search never
 * holds an opinion about what is being measured: the rules text against the height of its box,
 * a card name against the width of its plate.
 *
 * <p>Bisection rather than a fixed walk down from the top, and the reason is granularity rather
 * than speed. Content height rises with font size in jumps, not smoothly — the step that
 * rewraps a line adds a whole line at once — so a walk lands on whichever side of a jump it
 * happens to hit. Group Project fit two lines of rules text at 7px and four at 7.5px, and a
 * half-pixel walk therefore took 7px and left a fifth of the text box empty. Halving the
 * interval reaches a tenth of a pixel in the number of probes walking it reaches a half.
 *
 * <p>The returned size is always one that fit when it was applied, even if `applyAndTest` is
 * not monotonic in size. Bisection assumes it is, and for text it is; the closing check is
 * there so a violation degrades to a smaller size rather than to clipped text.
 */
export function largestFittingSize(
  minSize: number,
  maxSize: number,
  precision: number,
  applyAndTest: (size: number) => boolean,
): number | null {
  if (applyAndTest(maxSize)) {
    return maxSize;
  }
  if (!applyAndTest(minSize)) {
    return null;
  }

  let fitting = minSize;
  let tooBig = maxSize;
  while (tooBig - fitting > precision) {
    const mid = (fitting + tooBig) / 2;
    if (applyAndTest(mid)) {
      fitting = mid;
    } else {
      tooBig = mid;
    }
  }

  // Settle on the precision grid so the applied size is a round number rather than a bisection
  // remainder. Snapping only ever rounds down, so it cannot turn a fitting size into an
  // overflowing one — but it is re-tested anyway, because "cannot" here rests on the
  // monotonicity assumption and the cost of being wrong is clipped text.
  const snapped = Math.max(minSize, Math.floor(fitting / precision) * precision);
  const settled = Number(snapped.toFixed(4));
  if (!applyAndTest(settled)) {
    applyAndTest(fitting);
    return fitting;
  }
  return settled;
}
