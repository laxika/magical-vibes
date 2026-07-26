/**
 * Set symbols as Keyrune font glyphs.
 *
 * <p>Sibling of mana-symbols.ts and the same shape: a pure function from card data to class
 * names, no service and nothing to await. Keyrune is a second font from the same author as
 * Mana, carrying the expansion symbol of every set — see symbols.css.
 *
 * <p>What this replaces: a per-set fetch of an SVG from svgs.scryfall.io, cached in IndexedDB
 * and painted as a mask over a flat rarity colour, which meant a card's symbol arrived some
 * time after the card did, and needed a placeholder to sit in until it had. The font draws it
 * on the first frame instead, and that whole service is gone. A set the font has never heard of
 * prints its own code rather than reaching for the network again.
 */

/**
 * The sets Keyrune 3.19.0 has a glyph for, as Scryfall spells the code.
 *
 * <p>Generated from Keyrune's own stylesheet rather than written by hand: every `.ss-<code>`
 * class in it, minus the modifiers (`ss-2x`, `ss-rare`, `ss-grad` and the rest). Regenerate it
 * whenever the version pinned in symbols.css moves.
 *
 * <p>It has to be an explicit list, and this is the one thing about Keyrune worth remembering:
 * a bare `.ss` draws the MTG logo, so an unrecognised code renders a real, wrong symbol rather
 * than nothing at all. Without this list every set printed after 3.19.0 would quietly show that
 * logo in place of its own symbol — which is worse than the printed code it falls back to,
 * because it looks deliberate.
 *
 * <p>A handful of entries are not sets in the sense the card frame means — the guild-kit codes
 * (`azorius`, `boros`, …) and the Alchemy year buckets (`y22`…`y26`) among them. They are what
 * the font ships and no card's setCode ever equals them, so they are left in rather than
 * curated out: a hand-pruned copy would drift from the file it is generated from.
 */
const KEYRUNE_SETS: ReadonlySet<string> = new Set([
  '10e', '1e', '2e', '2ed', '2u', '2x2', '2xm', '30a', '3e', '3ed', '40k', '4ed', '5dn', '5ed',
  '6ed', '7ed', '8ed', '9ed', 'a25', 'acr', 'aer', 'afc', 'afr', 'akh', 'akr', 'ala', 'all',
  'ann', 'apc', 'arb', 'arc', 'arn', 'ath', 'atq', 'avr', 'azorius', 'bbd', 'bcore', 'bfz',
  'big', 'blb', 'blc', 'bng', 'bok', 'boros', 'bot', 'br', 'brb', 'brc', 'bro', 'brr', 'btd',
  'c13', 'c14', 'c15', 'c16', 'c17', 'c18', 'c19', 'c20', 'c21', 'cc1', 'cc2', 'chk', 'chr',
  'clb', 'clu', 'cm1', 'cm2', 'cma', 'cmc', 'cmd', 'cmm', 'cmr', 'cn2', 'cns', 'con', 'csp',
  'dd2', 'ddc', 'ddd', 'dde', 'ddf', 'ddg', 'ddh', 'ddi', 'ddj', 'ddk', 'ddl', 'ddm', 'ddn',
  'ddo', 'ddp', 'ddq', 'ddr', 'dds', 'ddt', 'ddu', 'dft', 'dgm', 'dimir', 'dis', 'dka', 'dkm',
  'dmc', 'dmr', 'dmu', 'dom', 'dpa', 'drb', 'drc', 'drk', 'dsc', 'dsk', 'dst', 'dtk', 'duels',
  'dvk', 'e01', 'e02', 'ea1', 'ecc', 'ecl', 'eld', 'ema', 'emn', 'eoc', 'eoe', 'eos', 'eve',
  'evg', 'exo', 'exp', 'fca', 'fdc', 'fdn', 'fem', 'fic', 'fin', 'fra', 'frf', 'fut', 'gk1',
  'gk2', 'gn2', 'gn3', 'gnt', 'golgari', 'gpt', 'grn', 'gruul', 'gs1', 'gtc', 'h09', 'h17',
  'ha1', 'hbg', 'hml', 'hob', 'hoc', 'hop', 'hou', 'htr', 'htr17', 'ice', 'ice2', 'iko', 'ima',
  'inr', 'inv', 'isd', 'izzet', 'j20', 'j21', 'j22', 'j25', 'j25a', 'jmp', 'jou', 'jud', 'khc',
  'khm', 'kld', 'klr', 'ktk', 'lcc', 'lci', 'lea', 'leb', 'leg', 'lgn', 'lrw', 'ltc', 'ltr',
  'm10', 'm11', 'm12', 'm13', 'm14', 'm15', 'm19', 'm20', 'm21', 'm3c', 'mar', 'mat', 'mb1',
  'mb2', 'mbs', 'md1', 'me1', 'me2', 'me3', 'me4', 'med', 'mh1', 'mh2', 'mh3', 'mic', 'mid',
  'mir', 'mkc', 'mkm', 'mm2', 'mm3', 'mma', 'mmq', 'moc', 'modo', 'mom', 'mor', 'mp1', 'mp2',
  'mps', 'mrd', 'msc', 'msh', 'mul', 'ncc', 'nec', 'nem', 'neo', 'nms', 'nph', 'ody', 'ogw',
  'om1', 'omb', 'onc', 'one', 'ons', 'ori', 'orzhov', 'otc', 'otj', 'otp', 'p02', 'papac',
  'parl', 'parl2', 'parl3', 'past', 'pbook', 'pc2', 'pca', 'pcy', 'pd2', 'pd3', 'pdep', 'pdrc',
  'peuro', 'pfnm', 'pgru', 'pheart', 'pidw', 'pio', 'pip', 'plc', 'pleaf', 'pls', 'pm2', 'pma',
  'pmei', 'pmodo', 'pmps', 'pmpu', 'pmtg1', 'pmtg2', 'po2', 'por', 'psalvat05', 'psalvat11',
  'psega', 'psld', 'psum', 'ptg', 'ptk', 'ptsa', 'pxbox', 'pz1', 'pz2', 'pza', 'rakdos', 'rav',
  'ren', 'rex', 'rin', 'rix', 'rna', 'roe', 'rtr', 'rvr', 's00', 's99', 'scd', 'scg',
  'selesnya', 'shm', 'simic', 'sir', 'sis', 'sld', 'sld2', 'slu', 'snc', 'soa', 'soc', 'soi',
  'sok', 'som', 'sos', 'spe', 'spg', 'spm', 'ss1', 'ss2', 'ss3', 'sta', 'sth', 'stx', 'tce',
  'td2', 'tdc', 'tdm', 'thb', 'ths', 'tla', 'tle', 'tmc', 'tmp', 'tmt', 'tor', 'tpr', 'tsp',
  'tsr', 'uds', 'ugl', 'ulg', 'uma', 'una', 'und', 'unf', 'unh', 'usg', 'ust', 'v09', 'v0x',
  'v10', 'v11', 'v12', 'v13', 'v14', 'v15', 'v16', 'v17', 'van', 'vis', 'vma', 'voc', 'vow',
  'w16', 'w17', 'war', 'who', 'woc', 'woe', 'wot', 'wth', 'wwk', 'x2ps', 'x4ea', 'xcle', 'xdnd',
  'xduels', 'xice', 'xkld', 'xlcu', 'xln', 'xmods', 'xren', 'xrin', 'xssm', 'y22', 'y23', 'y24',
  'y25', 'y26', 'yblb', 'ybro', 'ydft', 'ydmu', 'ydsk', 'yeoe', 'ylci', 'ymid', 'ymkm', 'yneo',
  'yone', 'yotj', 'ysnc', 'ytdm', 'ywoe', 'zen', 'znc', 'zne', 'znr',
]);

/**
 * The Keyrune classes drawing `setCode`'s expansion symbol, or null when the font has no glyph
 * for that set — in which case the caller prints the code itself.
 *
 * <p>Rarity is deliberately not folded in here. Keyrune ships `ss-common`/`ss-uncommon`/… of its
 * own, but they carry its palette rather than the frame's. Colour stays in the stylesheet, keyed
 * off data-rarity, so the glyph and the printed code are tinted by one rule and cannot drift.
 */
export function setSymbolClasses(setCode: string | null | undefined): string | null {
  if (!setCode) {
    return null;
  }
  const code = setCode.trim().toLowerCase();
  return KEYRUNE_SETS.has(code) ? `ss ss-${code}` : null;
}
