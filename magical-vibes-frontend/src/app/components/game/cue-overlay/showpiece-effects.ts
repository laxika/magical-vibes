import type { CueEffect } from '../../../services/cue-effect-player.service';
import { burst, drawParticles, hashSeed, seededRandom } from './cue-particles';
import { PALETTES } from './cue-palette';
import { drawCueAnimation } from './cue-animations';
import { CHAR_ASH } from './cue-card-motion';
import {
  Anchor, LOSS_COLOR, anchorOf, clamp01, coverParticles, drawFloatingText, impactParticles, isDamage, playCardExit,
  randomFor, recoil, ring, spawn, wash
} from './default-effects';

/**
 * Showpiece effects: hand-made effects for single cards, played instead of the defaults. The
 * table is keyed by card name, so every printing of a card shares its effect. A cue matches by
 * its `sourceName`, which a face-down source doesn't have, so a hidden card never gives itself
 * away and falls back to the default effects.
 */

/** How long Lightning Bolt's effect lasts at normal speed. */
export const LIGHTNING_BOLT_MS = 650;
/** When the bolt reaches its target, as a share of the effect. */
const BOLT_STRIKE = 0.18;

/** How long Doom Blade's effect lasts at normal speed. */
export const DOOM_BLADE_MS = 1300;
/** When the skull starts to form in the cloud, falls, and lands, as shares of the effect. */
const SKULL_FORMS = 0.2;
const SKULL_FALLS = 0.5;
const SKULL_LANDS = 0.64;

interface Point {
  x: number;
  y: number;
}

/**
 * A jagged lightning path from the top of the window down to the anchor, with two forks. The
 * same `flicker` gives the same path, so it changes a few times a second, not every frame.
 */
function boltPath(seed: number, flicker: number, at: Anchor, lean: number): { trunk: Point[]; forks: Point[][] } {
  const random = seededRandom(hashSeed(`${seed}:${flicker}`));
  const start = { x: at.x + lean * at.unit, y: -at.unit * 0.1 };
  const segments = Math.max(6, Math.round((at.y - start.y) / (at.unit * 0.35)));
  const trunk: Point[] = [start];
  for (let i = 1; i <= segments; i++) {
    const t = i / segments;
    const jitter = (random() * 2 - 1) * at.unit * 0.22;
    trunk.push(i === segments
      ? { x: at.x, y: at.y }
      : { x: start.x + (at.x - start.x) * t + jitter, y: start.y + (at.y - start.y) * t });
  }
  const forks = [0.4, 0.65].map(share => {
    const from = trunk[Math.floor(share * segments)];
    const side = random() < 0.5 ? -1 : 1;
    const fork: Point[] = [from];
    for (let i = 1; i <= 3; i++) {
      const previous = fork[i - 1];
      fork.push({ x: previous.x + side * at.unit * (0.12 + random() * 0.1), y: previous.y + at.unit * (0.12 + random() * 0.12) });
    }
    return fork;
  });
  return { trunk, forks };
}

/** Strokes the first `reach` (0..1) of a path in glowing layers, dark edge first and white core last. */
function strokeBolt(draw: CanvasRenderingContext2D, points: Point[], reach: number, unit: number, alpha: number): void {
  const length = (points.length - 1) * reach;
  const whole = Math.floor(length);
  draw.lineCap = 'round';
  draw.lineJoin = 'round';
  const layers: [string, number][] = [
    ['rgba(110, 30, 0, 0.35)', 0.14], [PALETTES.fire.colors[1], 0.09], [PALETTES.fire.colors[0], 0.05], ['rgb(255, 255, 255)', 0.022]
  ];
  for (const [color, width] of layers) {
    draw.globalAlpha = alpha;
    draw.strokeStyle = color;
    draw.lineWidth = Math.max(1.5, unit * width);
    draw.beginPath();
    draw.moveTo(points[0].x, points[0].y);
    for (let i = 1; i <= whole; i++) draw.lineTo(points[i].x, points[i].y);
    if (whole < points.length - 1) {
      const from = points[whole];
      const to = points[whole + 1];
      const part = length - whole;
      draw.lineTo(from.x + (to.x - from.x) * part, from.y + (to.y - from.y) * part);
    }
    draw.stroke();
  }
  draw.globalAlpha = 1;
}

/**
 * Lightning Bolt: a jagged bolt cracks down from the top of the window onto the target, the
 * window flashes and shakes, and the target recoils from a fiery hit with the damage dealt.
 */
export const LIGHTNING_BOLT_EFFECT: CueEffect = {
  matches: (cue, rects) => isDamage(cue) && rects.target !== null,
  async play(cue, rects, context) {
    const palette = PALETTES.fire;
    const random = randomFor(cue);
    const seed = Math.floor(random() * 1e9);
    const lean = (random() - 0.5) * 0.8;
    const amount = cue.amount ?? 0;
    const particles = impactParticles(random, palette, Math.max(4, amount));
    let struck = false;
    let recoiled = Promise.resolve();
    await context.animate(LIGHTNING_BOLT_MS, (draw, progress) => {
      const rect = rects.target;
      if (!rect) return;
      const at = anchorOf(rect);
      const fade = progress < BOLT_STRIKE ? 1 : 1 - clamp01((progress - BOLT_STRIKE) / 0.4);
      if (fade > 0) {
        const { trunk, forks } = boltPath(seed, Math.floor(progress * LIGHTNING_BOLT_MS / 60), at, lean);
        const reach = clamp01(progress / BOLT_STRIKE);
        strokeBolt(draw, trunk, reach, at.unit, fade);
        if (reach >= 1) forks.forEach(fork => strokeBolt(draw, fork, 1, at.unit * 0.6, fade * 0.8));
      }
      if (progress < BOLT_STRIKE) return;
      if (!struck) {
        struck = true;
        context.shake(Math.min(8, Math.max(3, at.unit * 0.06)), 240);
        // The bolt comes from the sky, not from whoever cast it, so the card shakes where it stands.
        recoiled = recoil(context, { source: null, target: rect, targetElement: rects.targetElement }, amount,
          LIGHTNING_BOLT_MS * (1 - progress));
      }
      const hit = (progress - BOLT_STRIKE) / (1 - BOLT_STRIKE);
      if (hit < 0.25) {
        draw.globalAlpha = 0.3 * (1 - hit / 0.25);
        draw.fillStyle = 'rgb(255, 244, 214)';
        draw.fillRect(0, 0, window.innerWidth, window.innerHeight);
        draw.globalAlpha = 1;
      }
      wash(draw, rect, palette.wash, 0.5 * (1 - hit));
      if (!drawCueAnimation(draw, 'impact', palette, hit, at, 1.1)) ring(draw, at, palette.accent, hit, 0.2, 1.1);
      drawParticles(draw, particles, hit, at.x, at.y, at.unit);
      if (amount > 0) drawFloatingText(draw, `-${amount}`, LOSS_COLOR, at, hit);
    });
    await recoiled;
  }
};

/** A plain skull for when the authored one isn't loaded. */
function drawSkull(draw: CanvasRenderingContext2D, x: number, y: number, size: number, eyeColor: string): void {
  const half = size / 2;
  draw.save();
  draw.translate(x, y);
  draw.fillStyle = 'rgb(236, 230, 242)';
  draw.strokeStyle = 'rgb(27, 16, 34)';
  draw.lineWidth = Math.max(2, size * 0.04);
  draw.beginPath();
  draw.ellipse(0, -half * 0.1, half * 0.75, half * 0.7, 0, 0, Math.PI * 2);
  draw.fill();
  draw.stroke();
  draw.beginPath();
  draw.rect(-half * 0.35, half * 0.45, half * 0.7, half * 0.3);
  draw.fill();
  draw.stroke();
  for (const [color, radius] of [['rgb(20, 10, 26)', 0.2], [eyeColor, 0.08]] as const) {
    draw.fillStyle = color;
    for (const side of [-1, 1]) {
      draw.beginPath();
      draw.ellipse(side * half * 0.3, 0, half * radius, half * radius * 1.1, 0, 0, Math.PI * 2);
      draw.fill();
    }
  }
  draw.restore();
}

/**
 * Doom Blade: a dark cloud gathers over the target, a skull forms in it and drops onto the
 * target, and the creature goes dark under a burst of violet smoke and burns away in violet.
 */
export const DOOM_BLADE_EFFECT: CueEffect = {
  matches: (cue, rects) => cue.type === 'DESTROYED' && rects.target !== null,
  async play(cue, rects, context) {
    const shadow = PALETTES.shadow;
    const random = randomFor(cue);
    const cloudSmoke = burst(random, {
      count: 26, mode: 'smoke', colors: shadow.colors, speed: [0.05, 0.2], size: [0.18, 0.3], grow: 1.6,
      life: [0.7, 1], delay: [0, 0.2], area: { x: 0.9, y: 0.25 }
    });
    const smoke = spawn(rects.target, at => coverParticles(random, shadow, at, 34, 'smoke')) ?? [];
    const sparks = impactParticles(random, shadow, 6);
    let landed = false;
    let card = { frame: (_progress: number) => {}, done: Promise.resolve() };
    await context.animate(DOOM_BLADE_MS, (draw, progress) => {
      const rect = rects.target;
      if (!rect) return;
      const at = anchorOf(rect);
      const cloud = { x: at.x, y: Math.max(at.unit * 0.5, at.y - (at.halfHeight + 0.45) * at.unit), unit: at.unit };

      draw.globalAlpha = progress < 0.75 ? 1 : 1 - (progress - 0.75) / 0.25;
      if (!drawCueAnimation(draw, 'doom-cloud', shadow, progress, cloud, 0.8)) {
        drawParticles(draw, cloudSmoke, Math.min(1, progress * 1.2), cloud.x, cloud.y, cloud.unit);
      }

      const afterLanding = clamp01((progress - SKULL_LANDS) / 0.12);
      if (progress >= SKULL_FORMS && afterLanding < 1) {
        const fall = clamp01((progress - SKULL_FALLS) / (SKULL_LANDS - SKULL_FALLS)) ** 3;
        const skull = { x: at.x, y: cloud.y + (at.y - cloud.y) * fall, unit: at.unit };
        const scale = 0.9 + 0.25 * fall + 0.5 * afterLanding;
        const forming = clamp01((progress - SKULL_FORMS) / (SKULL_LANDS - SKULL_FORMS));
        draw.globalAlpha = (1 - afterLanding) * Math.min(1, forming * 4);
        if (!drawCueAnimation(draw, 'skull', shadow, forming, skull, scale)) {
          drawSkull(draw, skull.x, skull.y, at.unit * 0.7 * scale, shadow.accent);
        }
      }
      draw.globalAlpha = 1;

      if (progress < SKULL_LANDS) return;
      if (!landed) {
        landed = true;
        context.shake(Math.min(6, Math.max(2, at.unit * 0.04)), 180);
        card = playCardExit(context, rects.targetElement, {
          motion: [{ translate: '0% 0%', opacity: 1 }, { offset: 0.97, opacity: 1 }, { translate: '0% 3%', opacity: 0 }],
          burn: { look: { rim: shadow.accent, ash: CHAR_ASH }, from: 0.05, to: 0.95, saturation: 0.5 }
        }, DOOM_BLADE_MS * (1 - progress), Math.floor(random() * 1000));
      }
      const hit = (progress - SKULL_LANDS) / (1 - SKULL_LANDS);
      card.frame(hit);
      wash(draw, rect, shadow.wash, 0.6 * Math.sin(Math.PI * Math.min(1, hit * 2.5)));
      ring(draw, at, shadow.accent, Math.min(1, hit * 2.5), 0.2, 1.1);
      drawParticles(draw, sparks, hit, at.x, at.y, at.unit);
      drawParticles(draw, smoke, hit, at.x, at.y, at.unit);
    });
    await card.done;
  }
};

/** The showpiece effects of each card, by card name. Add a card here to give it its own look. */
export const SHOWPIECES: Readonly<Record<string, readonly CueEffect[]>> = {
  'Lightning Bolt': [LIGHTNING_BOLT_EFFECT],
  'Doom Blade': [DOOM_BLADE_EFFECT]
};

/** Every showpiece, each matching only cues whose source has its card's name; for `CUE_EFFECTS`. */
export const SHOWPIECE_EFFECTS: readonly CueEffect[] = Object.entries(SHOWPIECES).flatMap(([name, effects]) =>
  effects.map((effect): CueEffect => ({
    matches: (cue, rects) => cue.sourceName === name && effect.matches(cue, rects),
    play: (cue, rects, context) => effect.play(cue, rects, context)
  })));
