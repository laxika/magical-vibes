/**
 * Card symbols — mana costs, tap, energy, watermarks — as Mana font glyphs.
 *
 * <p>Pure functions rather than a service, and that is the whole point of the file. These used to
 * be two injectable caches: one fetching every `{W}` from svgs.scryfall.io as its own SVG, one
 * fetching watermarks from a GitHub raw URL, both keeping blobs in IndexedDB and handing out
 * object URLs. Everything that consumed them had to cope with a symbol not being there yet — the
 * card frame's text fitter needed a version signal to re-run against, the draft list read that
 * signal by hand, and the fake in the card-display harness exists to reproduce the two-step
 * arrival faithfully enough to test. A glyph has none of that: the answer is the same on the
 * first call as on the thousandth, so there is nothing to invalidate and nothing to await.
 *
 * <p>What survives from that design is the failure mode worth keeping. An unrecognised symbol
 * renders as the literal `{FOO}` it came in as, not as a blank disc — the tables below are what
 * the font actually has glyphs for, so a symbol printed after this version of Mana degrades to
 * something a player can still read.
 */

/**
 * Every symbol Scryfall emits, mapped to the Mana class that draws it. Generated from
 * api.scryfall.com/symbology against mana-font 1.18.0 and checked glyph by glyph, so the
 * omissions below are the font's and not an oversight:
 *
 * <p>`{C/P}` (one colourless mana or two life, MOM) has no glyph in 1.18.0 and so has no entry.
 *
 * <p>The class names are mostly the symbol lowercased with the slashes taken out, which is not a
 * rule worth coding against — `{T}` is `ms-tap`, `{Q}` is `ms-untap`, `{A}` is `ms-acorn`, and
 * half mana is a modifier class rather than a glyph of its own. Spelling all eighty-three out
 * costs less than a derivation with four exceptions and a whitelist to catch the rest.
 */
const SYMBOL_CLASSES: Readonly<Record<string, string>> = {
  // Generic
  '{0}': 'ms-0', '{1}': 'ms-1', '{2}': 'ms-2', '{3}': 'ms-3', '{4}': 'ms-4',
  '{5}': 'ms-5', '{6}': 'ms-6', '{7}': 'ms-7', '{8}': 'ms-8', '{9}': 'ms-9',
  '{10}': 'ms-10', '{11}': 'ms-11', '{12}': 'ms-12', '{13}': 'ms-13', '{14}': 'ms-14',
  '{15}': 'ms-15', '{16}': 'ms-16', '{17}': 'ms-17', '{18}': 'ms-18', '{19}': 'ms-19',
  '{20}': 'ms-20', '{100}': 'ms-100', '{1000000}': 'ms-1000000',
  '{X}': 'ms-x', '{Y}': 'ms-y', '{Z}': 'ms-z',
  '{½}': 'ms-1-2', '{∞}': 'ms-infinity',

  // Coloured, colourless and snow
  '{W}': 'ms-w', '{U}': 'ms-u', '{B}': 'ms-b', '{R}': 'ms-r', '{G}': 'ms-g',
  '{C}': 'ms-c', '{S}': 'ms-s',

  // Half mana (Unhinged). ms-half clips a whole symbol rather than being one.
  '{HW}': 'ms-w ms-half', '{HR}': 'ms-r ms-half',

  // Hybrid
  '{W/U}': 'ms-wu', '{W/B}': 'ms-wb', '{U/B}': 'ms-ub', '{U/R}': 'ms-ur', '{B/R}': 'ms-br',
  '{B/G}': 'ms-bg', '{R/G}': 'ms-rg', '{R/W}': 'ms-rw', '{G/W}': 'ms-gw', '{G/U}': 'ms-gu',

  // Monocoloured hybrid
  '{2/W}': 'ms-2w', '{2/U}': 'ms-2u', '{2/B}': 'ms-2b', '{2/R}': 'ms-2r', '{2/G}': 'ms-2g',

  // Colourless hybrid
  '{C/W}': 'ms-cw', '{C/U}': 'ms-cu', '{C/B}': 'ms-cb', '{C/R}': 'ms-cr', '{C/G}': 'ms-cg',

  // Phyrexian
  '{P}': 'ms-p',
  '{W/P}': 'ms-wp', '{U/P}': 'ms-up', '{B/P}': 'ms-bp', '{R/P}': 'ms-rp', '{G/P}': 'ms-gp',

  // Hybrid Phyrexian
  '{W/U/P}': 'ms-wup', '{W/B/P}': 'ms-wbp', '{U/B/P}': 'ms-ubp', '{U/R/P}': 'ms-urp',
  '{B/R/P}': 'ms-brp', '{B/G/P}': 'ms-bgp', '{R/G/P}': 'ms-rgp', '{R/W/P}': 'ms-rwp',
  '{G/W/P}': 'ms-gwp', '{G/U/P}': 'ms-gup',

  // Everything else printed inside braces
  '{T}': 'ms-tap', '{Q}': 'ms-untap', '{E}': 'ms-e', '{PW}': 'ms-planeswalker',
  '{CHAOS}': 'ms-chaos', '{A}': 'ms-acorn', '{TK}': 'ms-tk', '{H}': 'ms-h',
  '{L}': 'ms-l', '{D}': 'ms-d',
};

/**
 * The watermarks Mana 1.18.0 has a glyph for, as Scryfall spells them in `Card.watermark`.
 *
 * <p>Scryfall's watermark field is an open enum — it grows with every set that prints one — so
 * this is a list of what can be drawn rather than of what can appear. A card whose watermark is
 * not here shows none, which is what the frame did before whenever the old fetch 404'd.
 *
 * <p>Deliberately short of what upstream draws. `set` — "use this set's own symbol" — is a
 * different font (Keyrune) and a different lookup, not a glyph Mana has. The rest of what is
 * left out is upstream's numbered and joke families (`herospath-1`…`-9`, the Un-set flavour
 * marks, the My Little Pony cutie marks), which are not spellings Scryfall's watermark field
 * ever takes.
 */
const WATERMARKS: ReadonlySet<string> = new Set([
  // Ravnica guilds
  'azorius', 'boros', 'dimir', 'golgari', 'gruul', 'izzet', 'orzhov', 'rakdos', 'selesnya',
  'simic',
  // Khans clans and Dragons of Tarkir broods
  'abzan', 'jeskai', 'mardu', 'sultai', 'temur',
  'atarka', 'dromoka', 'kolaghan', 'ojutai', 'silumgar',
  // Strixhaven colleges and New Capenna families
  'lorehold', 'prismari', 'quandrix', 'silverquill', 'witherbloom',
  'brokers', 'cabaretti', 'maestros', 'obscura', 'riveteers',
  // Factions and story marks
  'agentsofsneak', 'crossbreedlabs', 'goblinexplosioneers', 'leagueofdastardlydoom',
  'orderofthewidget', 'desparked', 'foretell', 'mirran', 'phyrexian', 'planeswalker',
  'colorpie', 'conspiracy', 'herospath',
  // Organised play, promo and crossover marks
  'arena', 'dci', 'dnd', 'fnm', 'grandprix', 'judgeacademy', 'junior', 'juniorapac',
  'junioreurope', 'japanjunior', 'mps', 'mtg', 'mtg10', 'mtg15', 'nerf', 'protour',
  'scholarship', 'transformers', 'wotc', 'wpn', 'corocoro', 'dengekimaoh', 'trumpkatsumai',
]);

/** Symbols are always braced, and never nested. */
const SYMBOL_PATTERN = /\{[^{}]+\}/g;

/**
 * Rewrites every `{…}` in `text` as a Mana glyph, leaving the rest of the string untouched.
 *
 * <p>The result is inserted with `bypassSecurityTrustHtml`, exactly as the `<img>` markup it
 * replaces was. Nothing here escapes the surrounding text, and nothing did before: it is oracle
 * text and engine-generated ability descriptions, both server-side, neither ever containing
 * markup.
 *
 * <p>`.mana-sym` is not upstream's — it is the hook the app's own rules use, for the drop shadow
 * that lifts a symbol off the name plate and for the size the browser and deck-builder cost
 * columns need. `.ms-cost` is what draws the coloured disc; without it the glyph is the bare
 * pictograph, the sun or the droplet with no circle around it.
 */
export function manaSymbolHtml(text: string): string {
  return text.replace(SYMBOL_PATTERN, symbol => {
    const classes = SYMBOL_CLASSES[symbol];
    if (!classes) {
      return symbol;
    }
    // aria-label rather than nothing, because the glyph is a private-use codepoint drawn by
    // ::before: to anything not painting the font — a screen reader, a copied selection — the
    // element is empty. `{W}` is what the card says and what the old <img> alt carried.
    return `<i class="mana-sym ms ms-cost ${classes}" role="img" aria-label="${symbol}"></i>`;
  });
}

/**
 * The Mana classes drawing `watermark`, or null when the font has no glyph for it — including
 * for a card with no watermark at all, which is nearly all of them.
 */
export function watermarkSymbolClasses(watermark: string | null | undefined): string | null {
  if (!watermark) {
    return null;
  }
  const name = watermark.trim().toLowerCase();
  return WATERMARKS.has(name) ? `ms ms-watermark-${name}` : null;
}
