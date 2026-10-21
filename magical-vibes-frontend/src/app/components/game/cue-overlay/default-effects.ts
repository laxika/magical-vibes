import type { CueEffect, CueEffectContext } from '../../../services/cue-effect-player.service';
import { VisualCue, VisualCueType } from '../../../services/websocket.service';
import { Particle, Random, burst, drawParticles, hashSeed, seededRandom, softDot, swirl } from './cue-particles';
import { CueElement, CuePalette, PALETTES, sourcePalette, sourcePaletteOr } from './cue-palette';
import { CueAnimationName, drawCueAnimation } from './cue-animations';

/**
 * The fallback effects: one for every cue type, chosen by type and source color (see
 * `cue-palette.ts`). Each lasts at most {@link DEFAULT_EFFECT_MAX_MS} at normal speed so long
 * chains of cues stay quick. They draw only shapes and numbers, never card names, so a hidden
 * source shows nothing but the neutral look.
 *
 * Each effect's centrepiece is an authored Lottie animation (`cue-animations.ts`) in the effect's
 * look, with the canvas particles and labels drawn over it. Until the animations have loaded (or
 * if they can't), the centrepiece falls back to a canvas drawing (rings, crosses, washes), so an
 * effect looks complete either way.
 */

/** The longest any default effect runs at normal speed, projectile included. */
export const DEFAULT_EFFECT_MAX_MS = 700;

export const PROJECTILE_MS = 220;
export const IMPACT_MS = 450;
export const LEAVE_MS = 600;
export const LIFE_MS = 550;
export const COUNTERS_MS = 550;
export const SPELL_CAST_MS = 400;
export const SPELL_RESOLVED_MS = 350;
export const SPELL_COUNTERED_MS = 450;

/** Damage numbers and life loss. */
export const LOSS_COLOR = 'rgb(255, 82, 70)';
/** Life gain and +1/+1 counters. */
const GAIN_COLOR = 'rgb(126, 240, 136)';

interface Point {
  x: number;
  y: number;
}

/** Where an effect is drawn: a point and the size its particles scale with. */
export interface Anchor extends Point {
  unit: number;
  /** Half the anchor's width and height, in units, for effects that cover the whole card. */
  halfWidth: number;
  halfHeight: number;
}

export function anchorOf(rect: DOMRect): Anchor {
  const unit = Math.max(24, Math.min(rect.width, rect.height));
  return {
    x: rect.left + rect.width / 2, y: rect.top + rect.height / 2, unit,
    halfWidth: rect.width / 2 / unit, halfHeight: rect.height / 2 / unit
  };
}

/** Where a cue with nothing on screen to land on plays: the middle of the window. */
export function fallbackAnchor(): Anchor {
  const unit = Math.min(140, Math.max(60, Math.min(window.innerWidth, window.innerHeight) / 6));
  return { x: window.innerWidth / 2, y: window.innerHeight / 2, unit, halfWidth: 0.5, halfHeight: 0.5 };
}

/** The same cue always draws the same particles. */
export function randomFor(cue: VisualCue): Random {
  return seededRandom(hashSeed([cue.type, cue.sourceCardId, cue.targetPermanentId, cue.targetPlayerId,
    cue.targetCardId, cue.amount, cue.counterType].join('|')));
}

export function easeOut(t: number): number {
  return 1 - (1 - t) ** 3;
}

/** Draws on the live rectangle every frame (it follows scrolling and zoom); skips frames where it is gone. */
export function on(rect: () => DOMRect | null, frame: (draw: CanvasRenderingContext2D, progress: number, at: Anchor) => void) {
  return (draw: CanvasRenderingContext2D, progress: number) => {
    const current = rect();
    if (current) frame(draw, progress, anchorOf(current));
  };
}

/** A tinted wash over the whole card that fades out. */
export function wash(draw: CanvasRenderingContext2D, rect: DOMRect | null, color: string, strength: number): void {
  if (!rect || strength <= 0) return;
  draw.globalAlpha = Math.min(1, strength);
  draw.fillStyle = color;
  draw.fillRect(rect.left, rect.top, rect.width, rect.height);
  draw.globalAlpha = 1;
}

/** An expanding (or, with `from > to`, contracting) ring that fades out. */
export function ring(draw: CanvasRenderingContext2D, at: Anchor, color: string, progress: number, from: number, to: number): void {
  const radius = (from + (to - from) * easeOut(progress)) * at.unit;
  draw.globalAlpha = 1 - progress;
  draw.strokeStyle = color;
  draw.lineWidth = Math.max(2, at.unit * 0.06);
  draw.beginPath();
  draw.arc(at.x, at.y, radius, 0, Math.PI * 2);
  draw.stroke();
  draw.globalAlpha = 1;
}

/** A number (or short label) that pops up over the anchor, rises and fades. */
export function drawFloatingText(draw: CanvasRenderingContext2D, text: string, color: string, at: Anchor, progress: number): void {
  const size = Math.round(Math.min(44, Math.max(18, at.unit * 0.42)));
  const pop = progress < 0.15 ? 0.6 + (progress / 0.15) * 0.55 : progress < 0.3 ? 1.15 - ((progress - 0.15) / 0.15) * 0.15 : 1;
  const y = at.y - easeOut(progress) * at.unit * 0.45;
  draw.save();
  draw.globalAlpha = progress < 0.6 ? 1 : 1 - (progress - 0.6) / 0.4;
  draw.translate(at.x, y);
  draw.scale(pop, pop);
  draw.font = `800 ${size}px system-ui, sans-serif`;
  draw.textAlign = 'center';
  draw.textBaseline = 'middle';
  draw.lineJoin = 'round';
  draw.lineWidth = Math.max(3, size * 0.16);
  draw.strokeStyle = 'rgba(0, 0, 0, 0.85)';
  draw.strokeText(text, 0, 0);
  draw.fillStyle = color;
  draw.fillText(text, 0, 0);
  draw.restore();
}

/**
 * A glowing bolt from `from` to `to` with a fading trail. Resolves at once when either end is not
 * on screen or they are the same card (a creature that damages itself).
 */
function fly(context: CueEffectContext, from: () => DOMRect | null, to: () => DOMRect | null,
             palette: CuePalette, durationMs: number): Promise<void> {
  const start = from();
  const end = to();
  if (!start || !end) return Promise.resolve();
  const a = anchorOf(start);
  const b = anchorOf(end);
  if (Math.hypot(a.x - b.x, a.y - b.y) < b.unit * 0.5) return Promise.resolve();
  return context.animate(durationMs, (draw, progress) => {
    const source = from();
    const target = to();
    if (!source || !target) return;
    const p = anchorOf(source);
    const q = anchorOf(target);
    const size = Math.max(5, q.unit * 0.12);
    for (let i = 7; i >= 0; i--) {
      const t = Math.max(0, progress * progress - i * 0.045);
      const x = p.x + (q.x - p.x) * t;
      const y = p.y + (q.y - p.y) * t;
      const radius = size * (1 - i / 9);
      draw.globalAlpha = 1 - i / 8;
      draw.fillStyle = softDot(draw, x, y, radius * 1.8, i === 0 ? 'rgb(255, 255, 255)' : palette.accent);
      draw.beginPath();
      draw.arc(x, y, radius * 1.8, 0, Math.PI * 2);
      draw.fill();
    }
    draw.globalAlpha = 1;
  });
}

/** The main particles of a palette, bursting outwards from the anchor. */
export function impactParticles(random: Random, palette: CuePalette, strength: number): Particle[] {
  return [
    ...burst(random, {
      count: 10 + Math.min(10, strength) * 2, mode: palette.mode, colors: palette.colors,
      speed: [0.3, 0.8], gravity: { x: 0, y: palette.rise }, size: [0.08, 0.16],
      grow: palette.mode === 'smoke' ? 2.2 : 0.4, life: [0.5, 0.9], delay: [0, 0.1]
    }),
    ...burst(random, {
      count: 8, mode: 'spark', colors: [palette.accent], speed: [0.7, 1.2], size: [0.03, 0.05],
      life: [0.3, 0.5]
    })
  ];
}

/** Particles that fill a whole card and drift the palette's way. */
export function coverParticles(random: Random, palette: CuePalette, at: Anchor, count: number, mode = palette.mode): Particle[] {
  return burst(random, {
    count, mode, colors: palette.colors, speed: [0.05, 0.25], direction: -Math.PI / 2, spread: Math.PI * 1.2,
    gravity: { x: 0, y: palette.rise }, size: [0.1, 0.2], grow: mode === 'smoke' ? 2 : 0.5,
    life: [0.5, 0.85], delay: [0, 0.3], area: { x: at.halfWidth * 0.85, y: at.halfHeight * 0.85 }
  });
}

/** Particles are spawned against the anchor as it is when the effect starts. */
export function spawn<T>(rect: DOMRect | null, make: (at: Anchor) => T): T | null {
  return rect ? make(anchorOf(rect)) : null;
}

function isType(...types: VisualCueType[]) {
  return (cue: VisualCue) => types.includes(cue.type);
}

export const isDamage = isType('DAMAGE_TO_PERMANENT', 'DAMAGE_TO_PLAYER');

/** Damage: a bolt from the source (when it is on screen) to the target, a starburst impact in the source's look, and the amount. */
export const DAMAGE_EFFECT: CueEffect = {
  matches: (cue, rects) => isDamage(cue) && rects.target !== null,
  async play(cue, rects, context) {
    const palette = sourcePalette(cue.sourceColors);
    const random = randomFor(cue);
    await fly(context, () => rects.source, () => rects.target, palette, PROJECTILE_MS);
    const amount = cue.amount ?? 0;
    const particles = impactParticles(random, palette, amount);
    await context.animate(IMPACT_MS, on(() => rects.target, (draw, progress, at) => {
      if (!drawCueAnimation(draw, 'impact', palette, progress, at, 0.9)) {
        ring(draw, at, palette.accent, Math.min(1, progress * 2), 0.15, 0.9);
      }
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
      if (amount > 0) drawFloatingText(draw, `-${amount}`, LOSS_COLOR, at, progress);
    }));
  }
};

/** How each way of leaving the battlefield moves its particles, and the look it uses without a colored source. */
interface LeaveStyle {
  palette: (cue: VisualCue) => CuePalette;
  /** Strength of the wash over the card at the start. */
  wash: number;
  particles: (random: Random, palette: CuePalette, at: Anchor) => Particle[];
  /** The authored centrepiece and its size in card units; without it loaded, only the wash and particles show. */
  animation: { name: CueAnimationName; scale: number };
}

const LEAVE_STYLES: Partial<Record<VisualCueType, LeaveStyle>> = {
  DESTROYED: {
    palette: cue => sourcePaletteOr(cue.sourceColors, 'shadow'),
    wash: 0.45,
    particles: (random, palette, at) => [
      ...coverParticles(random, palette, at, 30),
      ...impactParticles(random, palette, 4)
    ],
    animation: { name: 'crack', scale: 0.7 }
  },
  DIED: {
    palette: () => PALETTES.smoke,
    wash: 0.5,
    particles: (random, palette, at) => coverParticles(random, palette, at, 30, 'smoke'),
    animation: { name: 'wisp', scale: 0.8 }
  },
  SACRIFICED: {
    palette: cue => sourcePaletteOr(cue.sourceColors, 'shadow'),
    wash: 0.4,
    particles: (random, palette, at) => coverParticles(random, { ...palette, rise: -1.2 }, at, 26)
      .map(p => ({ ...p, y: p.y * 0.3 + at.halfHeight * 0.6 })),
    animation: { name: 'wisp', scale: 0.8 }
  },
  EXILED: {
    palette: cue => sourcePaletteOr(cue.sourceColors, 'light'),
    wash: 0.6,
    particles: (random, palette, at) => coverParticles(random, { ...palette, rise: -1.4 }, at, 30, 'glow'),
    animation: { name: 'pillar', scale: 0.9 }
  },
  RETURNED_TO_HAND: {
    palette: cue => sourcePaletteOr(cue.sourceColors, 'water'),
    wash: 0.35,
    particles: (random, palette, at) => swirl(random, {
      count: 28, mode: 'glow', colors: palette.colors, size: [0.06, 0.12], life: [0.6, 0.9], delay: [0, 0.15],
      radius: [Math.max(at.halfWidth, at.halfHeight) * 0.9, Math.max(at.halfWidth, at.halfHeight) * 1.3], turns: 1.2
    }),
    animation: { name: 'vortex', scale: 0.9 }
  }
};

/**
 * Leaving the battlefield, in the source's look: destroy cracks the card and bursts (black = smoke,
 * red = fire), exile flares up in a column of light, a bounce whirls into a vortex, a sacrifice
 * or a death without a source releases wisps and goes up in smoke.
 */
export const LEAVE_EFFECT: CueEffect = {
  matches: (cue, rects) => LEAVE_STYLES[cue.type] !== undefined && rects.target !== null,
  async play(cue, rects, context) {
    const style = LEAVE_STYLES[cue.type]!;
    const palette = style.palette(cue);
    const particles = spawn(rects.target, at => style.particles(randomFor(cue), palette, at)) ?? [];
    // Light that fades out brightens the card; everything else darkens or tints it.
    const washColor = cue.type === 'EXILED' ? PALETTES.light.wash : palette.wash;
    await context.animate(LEAVE_MS, (draw, progress) => {
      const rect = rects.target;
      if (!rect) return;
      const at = anchorOf(rect);
      wash(draw, rect, washColor, style.wash * Math.sin(Math.PI * Math.min(1, progress * 1.4)));
      drawCueAnimation(draw, style.animation.name, palette, progress, at, style.animation.scale);
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
    });
  }
};

/** Life gained or lost on a player's panel: a rising cross, light and `+N`, or a splashing drop, a falling drain and `-N`. */
export const LIFE_EFFECT: CueEffect = {
  matches: (cue, rects) => isType('LIFE_GAINED', 'LIFE_LOST')(cue) && rects.target !== null,
  async play(cue, rects, context) {
    const gained = cue.type === 'LIFE_GAINED';
    const palette = gained ? PALETTES.heal : PALETTES.drain;
    const amount = cue.amount ?? 0;
    const particles = spawn(rects.target, at => coverParticles(randomFor(cue), palette, at, 18 + Math.min(10, amount))) ?? [];
    await context.animate(LIFE_MS, on(() => rects.target, (draw, progress, at) => {
      drawCueAnimation(draw, gained ? 'life-gain' : 'life-loss', palette, progress, at, 1.2);
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
      if (amount > 0) drawFloatingText(draw, `${gained ? '+' : '-'}${amount}`, gained ? GAIN_COLOR : LOSS_COLOR, at, progress);
    }));
  }
};

const COUNTER_NUMBERS: Readonly<Record<string, number>> = { ZERO: 0, ONE: 1, TWO: 2 };

/** The label for `amount` counters of a type: `+2/+2` for two +1/+1 counters, `+1 lore` otherwise. */
export function counterLabel(counterType: string | null, amount: number): string {
  const pt = counterType ? /^(PLUS|MINUS)_(ZERO|ONE|TWO)_(PLUS|MINUS)_(ZERO|ONE|TWO)$/.exec(counterType) : null;
  if (pt) {
    const sign = (word: string) => word === 'PLUS' ? '+' : '-';
    return `${sign(pt[1])}${COUNTER_NUMBERS[pt[2]] * amount}/${sign(pt[3])}${COUNTER_NUMBERS[pt[4]] * amount}`;
  }
  return counterType ? `+${amount} ${counterType.toLowerCase().replace(/_/g, ' ')}` : `+${amount}`;
}

/** The look of a counter type: +X/+X counters grow green, -X/-X wither black, loyalty glows gold. */
export function counterElement(counterType: string | null, sourceColors: readonly string[]): CueElement {
  if (counterType?.startsWith('PLUS_')) return 'nature';
  if (counterType?.startsWith('MINUS_')) return 'shadow';
  if (counterType === 'LOYALTY') return 'gold';
  return sourcePalette(sourceColors).element;
}

/** Counters put on a permanent: a badge, particles in the counter type's look and the counters as a label. */
export const COUNTERS_EFFECT: CueEffect = {
  matches: (cue, rects) => cue.type === 'COUNTERS_ADDED' && rects.target !== null,
  async play(cue, rects, context) {
    const palette = PALETTES[counterElement(cue.counterType, cue.sourceColors)];
    const amount = cue.amount ?? 1;
    const label = counterLabel(cue.counterType, amount);
    const textColor = palette.element === 'nature' ? GAIN_COLOR : palette.element === 'shadow' ? 'rgb(196, 128, 240)' : palette.accent;
    const particles = spawn(rects.target, at => coverParticles(randomFor(cue), palette, at, 16 + Math.min(12, amount * 2))) ?? [];
    await context.animate(COUNTERS_MS, on(() => rects.target, (draw, progress, at) => {
      drawCueAnimation(draw, 'badge', palette, progress, at, 0.8);
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
      drawFloatingText(draw, label, textColor, at, progress);
    }));
  }
};

/** A spell is cast: a magic circle and a reticle close in on its target, or, without one, the circle flares in the middle of the window. */
export const SPELL_CAST_EFFECT: CueEffect = {
  matches: cue => cue.type === 'SPELL_CAST',
  async play(cue, rects, context) {
    const palette = sourcePalette(cue.sourceColors);
    const particles = burst(randomFor(cue), {
      count: 14, mode: 'glow', colors: palette.mode === 'smoke' ? [palette.accent] : palette.colors,
      speed: [0.4, 0.7], size: [0.05, 0.09], grow: 0.3, life: [0.5, 0.8], delay: [0.2, 0.35]
    });
    const targeted = rects.target !== null;
    await context.animate(SPELL_CAST_MS, (draw, progress) => {
      const rect = rects.target;
      if (targeted && !rect) return;
      const at = rect ? anchorOf(rect) : fallbackAnchor();
      if (rect) {
        if (!drawCueAnimation(draw, 'sigil', palette, progress, at, 1)) ring(draw, at, palette.accent, progress, 1.1, 0.5);
        ticks(draw, at, palette.accent, progress);
      } else if (!drawCueAnimation(draw, 'sigil', palette, progress, at, 1.2)) {
        ring(draw, at, palette.accent, progress, 0.2, 1);
      }
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
    });
  }
};

/** Four short marks that close in with the targeting ring. */
function ticks(draw: CanvasRenderingContext2D, at: Anchor, color: string, progress: number): void {
  const outer = (1.25 - 0.6 * easeOut(progress)) * at.unit;
  draw.globalAlpha = 1 - progress;
  draw.strokeStyle = color;
  draw.lineWidth = Math.max(2, at.unit * 0.05);
  draw.beginPath();
  for (let i = 0; i < 4; i++) {
    const angle = (Math.PI / 2) * i;
    draw.moveTo(at.x + Math.cos(angle) * outer, at.y + Math.sin(angle) * outer);
    draw.lineTo(at.x + Math.cos(angle) * (outer + at.unit * 0.18), at.y + Math.sin(angle) * (outer + at.unit * 0.18));
  }
  draw.stroke();
  draw.globalAlpha = 1;
}

/** A spell resolves: its stack entry flares in its look (the middle of the window when the stack is not showing). */
export const SPELL_RESOLVED_EFFECT: CueEffect = {
  matches: cue => cue.type === 'SPELL_RESOLVED',
  async play(cue, rects, context) {
    const palette = sourcePalette(cue.sourceColors);
    const particles = impactParticles(randomFor(cue), { ...palette, mode: 'glow', colors: palette.mode === 'smoke' ? [palette.accent] : palette.colors }, 2);
    const shown = rects.source !== null;
    await context.animate(SPELL_RESOLVED_MS, (draw, progress) => {
      const rect = rects.source;
      if (shown && !rect) return;
      const at = rect ? anchorOf(rect) : fallbackAnchor();
      wash(draw, rect, palette.wash, 0.35 * (1 - progress));
      if (!drawCueAnimation(draw, 'impact', palette, progress, at, 0.7)) ring(draw, at, palette.accent, progress, 0.3, 1.1);
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
    });
  }
};

/** A spell is countered: a bolt from the counter to the spell (when both show), then a rune over the spell cracks and shatters. */
export const SPELL_COUNTERED_EFFECT: CueEffect = {
  matches: cue => cue.type === 'SPELL_COUNTERED',
  async play(cue, rects, context) {
    const palette = sourcePaletteOr(cue.sourceColors, 'water');
    await fly(context, () => rects.source, () => rects.target, palette, PROJECTILE_MS);
    const particles = burst(randomFor(cue), {
      count: 22, mode: 'spark', colors: [palette.accent, 'rgb(255, 255, 255)'], speed: [0.6, 1.2],
      size: [0.03, 0.06], gravity: { x: 0, y: 0.8 }, life: [0.5, 0.9]
    });
    const shown = rects.target !== null;
    await context.animate(SPELL_COUNTERED_MS - PROJECTILE_MS, (draw, progress) => {
      const rect = rects.target;
      if (shown && !rect) return;
      const at = rect ? anchorOf(rect) : fallbackAnchor();
      wash(draw, rect, palette.wash, 0.4 * (1 - progress));
      if (!drawCueAnimation(draw, 'shatter', palette, progress, at, 0.9)) cross(draw, at, palette.accent, progress);
      drawParticles(draw, particles, progress, at.x, at.y, at.unit);
    });
  }
};

/** An X struck through the anchor, drawn in quickly and fading. */
function cross(draw: CanvasRenderingContext2D, at: Anchor, color: string, progress: number): void {
  const reach = at.unit * 0.45 * easeOut(Math.min(1, progress * 3));
  draw.globalAlpha = 1 - progress;
  draw.lineCap = 'round';
  for (const [width, style] of [[at.unit * 0.12, 'rgba(0, 0, 0, 0.6)'], [at.unit * 0.07, color]] as const) {
    draw.strokeStyle = style;
    draw.lineWidth = Math.max(2, width);
    draw.beginPath();
    draw.moveTo(at.x - reach, at.y - reach);
    draw.lineTo(at.x + reach, at.y + reach);
    draw.moveTo(at.x + reach, at.y - reach);
    draw.lineTo(at.x - reach, at.y + reach);
    draw.stroke();
  }
  draw.globalAlpha = 1;
}

/** The default effects, for `CUE_EFFECTS`; together they cover every cue type. */
export const DEFAULT_CUE_EFFECTS: readonly CueEffect[] = [
  DAMAGE_EFFECT,
  LEAVE_EFFECT,
  LIFE_EFFECT,
  COUNTERS_EFFECT,
  SPELL_CAST_EFFECT,
  SPELL_RESOLVED_EFFECT,
  SPELL_COUNTERED_EFFECT
];
