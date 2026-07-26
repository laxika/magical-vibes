/**
 * The inline markup a loaded mana symbol renders as.
 *
 * <p>Its own file, and its own test, because of what happens when the width is left implicit.
 * An `<img>` sized only by height has no width until the image loads and supplies an intrinsic
 * ratio — `width: auto` resolves to zero until then. `Flashback {4}{W}{W}` therefore measured
 * 61.7px on insertion and 94.7px once its three symbols decoded, a 33px shift on a text box
 * 137px wide. CardDisplayComponent fits rules text by measuring the rendered box, so it sized
 * the card against a line a quarter narrower than it would end up, the line rewrapped when the
 * symbols painted, and the text spilled past the bottom of the frame — visible only on cards
 * whose rules text carries a symbol, which is what made it look like a fitting bug rather than
 * a markup one.
 *
 * <p>`aspect-ratio` rather than a fixed width so the square is derived from whatever height
 * applies: the card frame sets 1em, the browser and deck-builder cost columns override it to
 * 14px, and both stay square and stable. Scryfall's card symbols are square at source (150x150).
 */
export function manaSymbolImg(url: string, alt: string): string {
  return `<img class="mana-sym" src="${url}" alt="${alt}"`
      + ` style="height:1em;aspect-ratio:1;vertical-align:middle;margin:0 1px;">`;
}
