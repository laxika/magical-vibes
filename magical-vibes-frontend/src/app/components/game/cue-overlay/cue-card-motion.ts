import { Random } from './cue-particles';

/**
 * Effects on the card itself rather than over it: the canvas overlay can't change how a card
 * looks, so these animate the card's own element (or copies of it) with the Web Animations API.
 *
 * - **Motion** uses the individual `translate`, `rotate` and `scale` properties, which apply on top
 *   of the card's own `transform` (tap rotation, combat lift), plus `opacity` and `filter`.
 * - **Held frames:** a card that leaves the battlefield keeps its last (invisible) frame until the
 *   state the cues lead to removes it, so it doesn't pop back between its effect and the new state.
 *   The anchor directive lets go of held frames when its element starts showing something else
 *   (stack entries are tracked by position), and a timer lets go if the element is still there
 *   long after every cue could have played.
 * - **Burn** ({@link createCardBurn}) is an SVG filter that eats the card away along a noise front
 *   from its edges inwards, with a glowing rim and a charred band behind it.
 * - **Shatter** ({@link shatter}) lays clipped copies of an element over it, one per shard.
 */

/** The Web Animations id of an animation whose last frame is held; see {@link releaseCueHolds}. */
export const CUE_HOLD_ANIMATION_ID = 'cue-hold';

/** How to animate an element; durations are at normal speed when passed to an effect context. */
export interface CueElementAnimation {
  durationMs: number;
  easing?: string;
  /** The element is leaving: keep its last frame afterwards, and jump to it when playback stops early. */
  hold?: boolean;
}

/**
 * Plays `keyframes` on `element`; resolves when they are done or `signal` aborts. Aborting cancels
 * the animation, or for a held one finishes it. A held frame is let go after `releaseAfterMs`
 * more if the element is still on the page. Does nothing where the Web Animations API is missing.
 */
export function animateCueElement(element: HTMLElement, keyframes: Keyframe[], options: CueElementAnimation,
                                  signal: AbortSignal, releaseAfterMs: number): Promise<void> {
  if (typeof element.animate !== 'function' || signal.aborted || options.durationMs <= 0) return Promise.resolve();
  const animation = element.animate(keyframes, {
    duration: options.durationMs,
    easing: options.easing ?? 'linear',
    fill: options.hold ? 'forwards' : 'none',
    id: options.hold ? CUE_HOLD_ANIMATION_ID : ''
  });
  if (options.hold) {
    setTimeout(() => {
      if (element.isConnected) animation.cancel();
    }, options.durationMs + releaseAfterMs);
  }
  return new Promise<void>(resolve => {
    animation.finished.then(() => resolve(), () => resolve());
    signal.addEventListener('abort', () => options.hold ? animation.finish() : animation.cancel(), { once: true });
  });
}

/** Lets go of every held last frame on `element`, so it shows as it is again. */
export function releaseCueHolds(element: HTMLElement): void {
  element.getAnimations?.().filter(animation => animation.id === CUE_HOLD_ANIMATION_ID).forEach(animation => animation.cancel());
}

/**
 * How much bigger an element is on screen than in its own CSS pixels (zoomed boards, the phone
 * layout), so a motion can be given in screen pixels. Rotation-proof for tapped cards.
 */
export function screenScale(element: HTMLElement, rect: DOMRect): number {
  const own = Math.max(element.offsetWidth, element.offsetHeight);
  return own > 0 ? Math.max(rect.width, rect.height) / own : 1;
}

/** The red flash a card takes when it is hit. */
const HIT_FLASH = 'sepia(0.8) saturate(4) hue-rotate(-45deg) brightness(1.05)';

/**
 * A card or player panel recoiling from a hit, `distancePx` of its own pixels: knocked along
 * `direction` (a unit vector from the source) and springing back, or shaken side to side
 * without one, flashing red either way.
 */
export function recoilKeyframes(direction: { x: number; y: number } | null, distancePx: number): Keyframe[] {
  const steps = direction
    ? [[0, 0], [0.12, 1], [0.35, -0.3], [0.6, 0.1], [1, 0]]
    : [[0, 0], [0.1, 1], [0.25, -0.8], [0.4, 0.5], [0.55, -0.2], [1, 0]];
  const along = direction ?? { x: 1, y: 0 };
  return steps.map(([offset, k]) => ({
    offset,
    translate: `${(along.x * k * distancePx).toFixed(2)}px ${(along.y * k * distancePx).toFixed(2)}px`,
    filter: offset > 0 && offset <= 0.15 ? HIT_FLASH : 'none'
  }));
}

/** How a card burns: the glowing rim along the front, and the color the band behind it chars to. */
export interface BurnLook {
  rim: string;
  /** Added to the darkened card in the band behind the rim, as `[r, g, b]` in 0..1. */
  ash: readonly [number, number, number];
}

/** Charred brown-black, for fire, shadow and most burns. */
export const CHAR_ASH = [0.06, 0.02, 0] as const;
/** Pale gold, for a card that goes up in light. */
export const GOLD_ASH = [0.75, 0.62, 0.3] as const;

/** Where the burn starts and ends on the noise field, and how wide the rim and the char band are. */
const BURN_FROM = 0.2;
const BURN_TO = 1.12;
const RIM = 0.05;
const CHAR_BAND = 0.06;
/** How sharp each threshold is (alpha per unit of the field). */
const SLOPE = 25;

/**
 * The noise-field thresholds at `burn` (0..1): below the first the card is gone, up to the second
 * it glows as rim, up to the third it is charred, above it is untouched. The field runs from about
 * 0.35 at the card's edges to about 1.05 in its middle, so 0 leaves the card whole and 1 burns it all.
 */
export function burnThresholds(burn: number): [number, number, number] {
  const gone = BURN_FROM + (BURN_TO - BURN_FROM) * Math.min(1, Math.max(0, burn));
  return [gone, gone + RIM, gone + RIM + CHAR_BAND];
}

/** A burn filter on the page: apply `filter` to the card, drive it with `set`, and `remove` it when done. */
export interface CardBurn {
  /** The CSS `filter` value that applies it. */
  readonly filter: string;
  /** How far the card has burnt (0 whole, 1 gone), and how bleached towards white and how saturated it is. */
  set(burn: number, tone?: { bleach?: number; saturation?: number }): void;
  remove(): void;
}

const SVG = 'http://www.w3.org/2000/svg';
let defs: SVGDefsElement | null = null;
let nextFilterId = 0;

/** The hidden `<defs>` burn filters live in, created on first use. */
function filterDefs(): SVGDefsElement {
  if (defs?.isConnected) return defs;
  const svg = document.createElementNS(SVG, 'svg');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('focusable', 'false');
  // display: none would switch the filters off; a sizeless box out of flow keeps them working.
  svg.setAttribute('style', 'position: absolute; width: 0; height: 0; overflow: hidden; pointer-events: none');
  defs = document.createElementNS(SVG, 'defs');
  svg.appendChild(defs);
  document.body.appendChild(svg);
  return defs;
}

function node(parent: Element, tag: string, attributes: Record<string, string | number>): SVGElement {
  const element = document.createElementNS(SVG, tag);
  for (const [name, value] of Object.entries(attributes)) element.setAttribute(name, String(value));
  parent.appendChild(element);
  return element;
}

/**
 * Adds a burn filter in `look` to the page. Its noise is fixed by `seed`, so a cue always burns
 * the same way. The front is noise plus the card's blurred outline, so the edges go first.
 */
export function createCardBurn(seed: number, look: BurnLook): CardBurn {
  const id = `cue-burn-${nextFilterId++}`;
  const filter = node(filterDefs(), 'filter', {
    id, x: '-15%', y: '-15%', width: '130%', height: '130%', 'color-interpolation-filters': 'sRGB'
  });
  const saturate = node(filter, 'feColorMatrix', { in: 'SourceGraphic', type: 'saturate', values: 1, result: 'saturated' });
  const bleach = node(filter, 'feComponentTransfer', { in: 'saturated', result: 'toned' });
  const channels = ['R', 'G', 'B'].map(channel => node(bleach, `feFunc${channel}`, { type: 'linear', slope: 1, intercept: 0 }));
  node(filter, 'feTurbulence', { type: 'fractalNoise', baseFrequency: 0.04, numOctaves: 3, seed, result: 'noise' });
  node(filter, 'feColorMatrix', {
    in: 'noise', type: 'matrix', values: '0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  1 0 0 0 0', result: 'grain'
  });
  node(filter, 'feGaussianBlur', { in: 'SourceAlpha', stdDeviation: 30, result: 'depth' });
  node(filter, 'feComposite', { in: 'grain', in2: 'depth', operator: 'arithmetic', k2: 0.6, k3: 0.6, result: 'field' });
  const masks = ['rim', 'char', 'card'].map(name => {
    const transfer = node(filter, 'feComponentTransfer', { in: 'field', result: `${name}Mask` });
    return node(transfer, 'feFuncA', { type: 'linear', slope: SLOPE, intercept: 0 });
  });
  node(filter, 'feFlood', { 'flood-color': look.rim, result: 'rimColor' });
  node(filter, 'feComposite', { in: 'rimColor', in2: 'rimMask', operator: 'in', result: 'rimArea' });
  node(filter, 'feComposite', { in: 'rimArea', in2: 'SourceAlpha', operator: 'in', result: 'rim' });
  node(filter, 'feComposite', { in: 'rim', in2: 'charMask', operator: 'out', result: 'front' });
  node(filter, 'feGaussianBlur', { in: 'front', stdDeviation: 5, result: 'glow' });
  const [r, g, b] = look.ash;
  node(filter, 'feColorMatrix', {
    in: 'toned', type: 'matrix', values: `0.22 0 0 0 ${r}  0 0.16 0 0 ${g}  0 0 0.14 0 ${b}  0 0 0 1 0`, result: 'charred'
  });
  node(filter, 'feComposite', { in: 'charred', in2: 'charMask', operator: 'in', result: 'char' });
  node(filter, 'feComposite', { in: 'toned', in2: 'cardMask', operator: 'in', result: 'card' });
  const merge = node(filter, 'feMerge', {});
  ['glow', 'rim', 'char', 'card'].forEach(name => node(merge, 'feMergeNode', { in: name }));

  const burn: CardBurn = {
    filter: `url(#${id})`,
    set(amount, tone = {}) {
      burnThresholds(amount).forEach((threshold, i) => masks[i].setAttribute('intercept', String(-SLOPE * threshold)));
      saturate.setAttribute('values', String(tone.saturation ?? 1));
      const white = tone.bleach ?? 0;
      channels.forEach(channel => {
        channel.setAttribute('slope', String(1 - white));
        channel.setAttribute('intercept', String(white));
      });
    },
    remove: () => filter.remove()
  };
  burn.set(0);
  return burn;
}

type Point = readonly [number, number];

/** One shard of a shattered element: its outline and where it breaks from, in percent of the element. */
export interface ShardShape {
  points: Point[];
  centroid: Point;
}

/** A point `s` along the element's outline, clockwise from the top-left corner (0..4, one per side). */
function onOutline(s: number): Point {
  const side = Math.floor(s) % 4;
  const along = (s - Math.floor(s)) * 100;
  return ([[along, 0], [100, along], [100 - along, 100], [0, 100 - along]] as const)[side];
}

/**
 * `count` shards that tile the element: wedges from a break point near the middle to jittered
 * points around the outline, each taking the corners between its two outline points.
 */
export function shardShapes(random: Random, count: number): ShardShape[] {
  const center: Point = [50 + (random() - 0.5) * 20, 45 + (random() - 0.5) * 20];
  const cuts = Array.from({ length: count }, (_, i) => (i + 0.5 + (random() - 0.5) * 0.6) * 4 / count).sort((a, b) => a - b);
  return cuts.map((from, i) => {
    const to = i + 1 < cuts.length ? cuts[i + 1] : cuts[0] + 4;
    const corners = [1, 2, 3, 4, 5, 6, 7].filter(corner => corner > from && corner < to).map(onOutline);
    const points = [center, onOutline(from), ...corners, onOutline(to)];
    const centroid: Point = [
      points.reduce((sum, [x]) => sum + x, 0) / points.length,
      points.reduce((sum, [, y]) => sum + y, 0) / points.length
    ];
    return { points, centroid };
  });
}

/** A copy of the element clipped to one shard, and how it flies off. */
export interface Shard {
  element: HTMLElement;
  keyframes: Keyframe[];
}

/**
 * Lays one clipped copy of `element` over it per shard, so the caller can hide the element and
 * fly the shards apart; `remove` takes them off the page. The copies sit next to the element, so
 * its styles still apply, and are placed by measuring rather than by layout math, so zoom and
 * transforms on the way don't matter. Null when the element has no box.
 */
export function shatter(element: HTMLElement, random: Random, count = 9): { shards: Shard[]; remove: () => void } | null {
  const rect = element.getBoundingClientRect();
  if (rect.width <= 0 || rect.height <= 0 || !element.parentElement) return null;
  const copy = () => {
    const shard = element.cloneNode(true) as HTMLElement;
    shard.removeAttribute('id');
    shard.querySelectorAll('[id]').forEach(child => child.removeAttribute('id'));
    shard.setAttribute('aria-hidden', 'true');
    Object.assign(shard.style, {
      position: 'absolute', left: '0px', top: '0px', margin: '0', boxSizing: 'border-box',
      width: `${element.offsetWidth}px`, height: `${element.offsetHeight}px`,
      pointerEvents: 'none', transition: 'none', animation: 'none', zIndex: '5'
    });
    element.before(shard);
    return shard;
  };
  // Measure where a copy lands at two offsets, then put every copy exactly over the element.
  const probe = copy();
  const origin = probe.getBoundingClientRect();
  probe.style.left = '100px';
  const per100 = (probe.getBoundingClientRect().left - origin.left) / 100 || 1;
  const left = `${(rect.left - origin.left) / per100}px`;
  const top = `${(rect.top - origin.top) / per100}px`;
  probe.remove();

  const shards = shardShapes(random, count).map(({ points, centroid }, i) => {
    const shard = copy();
    shard.style.left = left;
    shard.style.top = top;
    shard.style.clipPath = `polygon(${points.map(([x, y]) => `${x.toFixed(2)}% ${y.toFixed(2)}%`).join(', ')})`;
    const dx = centroid[0] - 50;
    const dy = centroid[1] - 50;
    const length = Math.hypot(dx, dy) || 1;
    const reach = 18 + random() * 22;
    const fall = 25 + random() * 20;
    const spin = (random() - 0.5) * 70 + (i % 2 === 0 ? 15 : -15);
    const x = (dx / length) * reach;
    const y = (dy / length) * reach;
    return {
      element: shard,
      keyframes: [
        { offset: 0, translate: '0% 0%', rotate: '0deg', opacity: 1 },
        { offset: 0.12, translate: `${(x * 0.08).toFixed(2)}% ${(y * 0.08).toFixed(2)}%`, rotate: '0deg', opacity: 1 },
        { offset: 0.6, opacity: 1 },
        { offset: 1, translate: `${x.toFixed(2)}% ${(y + fall).toFixed(2)}%`, rotate: `${spin.toFixed(1)}deg`, opacity: 0 }
      ]
    };
  });
  return { shards, remove: () => shards.forEach(shard => shard.element.remove()) };
}
