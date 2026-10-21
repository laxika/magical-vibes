/**
 * A tiny builder for Lottie (bodymovin 5) JSON: shape layers, groups, shapes, fills, strokes,
 * trim paths, repeaters and keyframed properties. Only what the cue animations use, and nothing
 * that needs expressions, images or fonts, so the files play in lottie-web's light canvas build.
 *
 * Coordinates are composition pixels. By convention 100 px is one "card unit" (the smaller side of
 * the card or panel an effect lands on), so a 200 x 200 composition covers twice a card's width.
 *
 * Colors are either fixed (`'#rrggbb'`, optionally `'#rrggbbaa'`) or a palette slot (`'@accent'`,
 * `'@c0'`, `'@c1'`, `'@c2'`). A slot is written as the fill's or stroke's `nm`, with a neutral
 * stand-in color; the client replaces it with the colors of the cue's look when it loads the file.
 */

export const FPS = 60;

/** Neutral stand-ins for palette slots, so a file still looks sensible in a Lottie previewer. */
const SLOT_PREVIEW = {
  '@accent': '#dce2ff',
  '@c0': '#eceefa',
  '@c1': '#bac4de',
  '@c2': '#8a94b8'
};

/** Cubic-bezier easings as Lottie keyframe tangents: `o` leaves this key, `i` enters the next one. */
const EASE = {
  linear: [[0.167, 0.167], [0.833, 0.833]],
  out: [[0.2, 0.9], [0.3, 1]],
  in: [[0.6, 0], [0.9, 0.4]],
  inOut: [[0.45, 0], [0.55, 1]],
  back: [[0.3, 1.6], [0.5, 1]],
  hold: null
};

function rgba(hex) {
  const value = hex.replace('#', '');
  const channel = i => Math.round(parseInt(value.slice(i, i + 2), 16) / 255 * 1000) / 1000;
  return [channel(0), channel(2), channel(4), value.length === 8 ? channel(6) : 1];
}

function colorOf(color) {
  return color.startsWith('@') ? { nm: color, c: rgba(SLOT_PREVIEW[color]) } : { nm: 'fixed', c: rgba(color) };
}

const asArray = value => Array.isArray(value) ? value : [value];

/** A static property. */
export const v = k => ({ a: 0, k });

/**
 * A keyframed property from `[frame, value, easing?]` triples; the easing applies from that key to
 * the next (default `inOut`, `hold` jumps).
 */
export function keys(...frames) {
  return {
    a: 1,
    k: frames.map(([t, value, ease = 'inOut'], index) => {
      const key = { t, s: asArray(value) };
      if (index === frames.length - 1) return key;
      if (ease === 'hold') return { ...key, h: 1 };
      const [[ox, oy], [ix, iy]] = EASE[ease];
      return { ...key, o: { x: [ox], y: [oy] }, i: { x: [ix], y: [iy] } };
    })
  };
}

const prop = (value, fallback) => value === undefined ? v(fallback) : (value.a !== undefined ? value : v(value));

/** A group transform (`tr`). Positions are relative to the layer's anchor. */
export function transform({ p = [0, 0], a = [0, 0], s = [100, 100], r = 0, o = 100 } = {}) {
  return { ty: 'tr', nm: 'Transform', p: prop(p), a: prop(a), s: prop(s), r: prop(r), o: prop(o), sk: v(0), sa: v(0) };
}

export function group(nm, items, tr = {}) {
  return { ty: 'gr', nm, it: [...items, transform(tr)] };
}

export function ellipse(size, p = [0, 0]) {
  return { ty: 'el', nm: 'Ellipse', d: 1, p: prop(p), s: prop(Array.isArray(size) || size.a !== undefined ? size : [size, size]) };
}

export function rect(size, { p = [0, 0], r = 0 } = {}) {
  return { ty: 'rc', nm: 'Rect', d: 1, p: prop(p), s: prop(size), r: prop(r) };
}

/** A star (`points` outer points) or, with `inner` null, a regular polygon. */
export function star(points, outer, inner = null, { p = [0, 0], r = 0, roundness = 0 } = {}) {
  const shape = { ty: 'sr', nm: 'Star', d: 1, sy: inner === null ? 2 : 1, pt: v(points), p: prop(p), r: prop(r),
    or: prop(outer), os: v(roundness) };
  return inner === null ? shape : { ...shape, ir: prop(inner), is: v(roundness) };
}

/** A path through `points`; straight segments, or Catmull-Rom smooth ones with `smooth`. */
export function path(points, { closed = true, smooth = 0 } = {}) {
  const count = points.length;
  const tangent = index => {
    if (!smooth || (!closed && (index === 0 || index === count - 1))) return [0, 0];
    const before = points[(index - 1 + count) % count];
    const after = points[(index + 1) % count];
    return [(after[0] - before[0]) * smooth / 2, (after[1] - before[1]) * smooth / 2];
  };
  const tangents = points.map((_, index) => tangent(index));
  return {
    ty: 'sh', nm: 'Path', d: 1,
    ks: v({ c: closed, v: points, i: tangents.map(([x, y]) => [-x, -y]), o: tangents })
  };
}

export function fill(color, opacity = 100) {
  const { nm, c } = colorOf(color);
  return { ty: 'fl', nm, c: v(c), o: prop(opacity), r: 1 };
}

export function stroke(color, width, { opacity = 100, cap = 'round' } = {}) {
  const { nm, c } = colorOf(color);
  return { ty: 'st', nm, c: v(c), o: prop(opacity), w: prop(width), lc: cap === 'round' ? 2 : 1, lj: 2, ml: 4 };
}

/** Trims the paths above it to `start`..`end` percent. */
export function trim(start, end, offset = 0) {
  return { ty: 'tm', nm: 'Trim', s: prop(start), e: prop(end), o: prop(offset), m: 1 };
}

/** Repeats everything above it `copies` times, each copy transformed by `tr` from the previous one. */
export function repeater(copies, { p = [0, 0], r = 0, s = [100, 100], startOpacity = 100, endOpacity = 100 } = {}) {
  return {
    ty: 'rp', nm: 'Repeater', c: prop(copies), o: v(0), m: 1,
    tr: { ty: 'tr', p: prop(p), a: v([0, 0]), s: prop(s), r: prop(r), so: v(startOpacity), eo: v(endOpacity) }
  };
}

/**
 * A shape layer whose origin is at `p`, relative to the middle of the composition. `shapes` are
 * listed top first, like in After Effects: a fill or stroke paints the paths above it, and the
 * first item draws on top.
 */
export function layer(nm, shapes, { ip = 0, op, p = [0, 0], s = [100, 100], r = 0, o = 100 } = {}) {
  return {
    ddd: 0, ty: 4, nm, sr: 1, ao: 0, bm: 0, ip, op, st: 0,
    ks: { o: prop(o), r: prop(r), p: prop(p), a: v([0, 0, 0]), s: prop(s) },
    shapes
  };
}

/**
 * A composition. `layers` are listed top first. Layers without `op` last the whole animation, and
 * layer positions are relative to the middle of the composition.
 */
export function composition(nm, { width = 200, height = 200, frames }, layers) {
  const middle = [width / 2, height / 2];
  return {
    v: '5.7.4', nm, fr: FPS, ip: 0, op: frames, w: width, h: height, ddd: 0, assets: [], markers: [],
    layers: layers.map((entry, index) => ({
      ...entry,
      ind: index + 1,
      op: entry.op ?? frames,
      ks: { ...entry.ks, p: mapValue(entry.ks.p ?? v([0, 0]), ([x, y]) => [x + middle[0], y + middle[1], 0]),
        s: mapValue(entry.ks.s, ([x, y]) => [x, y, 100]) }
    }))
  };
}

/** Applies `map` to a property's static value or to every keyframe's value. */
function mapValue(property, map) {
  if (property.a === 0) return v(map(property.k));
  return { ...property, k: property.k.map(key => ({ ...key, s: map(key.s) })) };
}

/** Rounds every number to 3 decimals, so the files stay small and diffs stay readable. */
export function compact(value) {
  if (typeof value === 'number') return Math.round(value * 1000) / 1000;
  if (Array.isArray(value)) return value.map(compact);
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([key, entry]) => [key, compact(entry)]));
  }
  return value;
}
