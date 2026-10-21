/**
 * A small, stateless particle system for cue effects. A particle's whole flight is fixed when it
 * is spawned, and every frame computes where it is from the animation's progress alone, so
 * effects look the same at any frame rate and speed. Positions, speeds and sizes are in units of
 * the anchor's size (the smaller side of the card or panel the particles belong to), so a burst
 * fills a phone-sized card the way it fills a desktop one.
 */

/** How a particle is drawn: a soft dot (`glow` for bright looks, `smoke` for dark ones) or a streak along its motion. */
export type ParticleMode = 'glow' | 'smoke' | 'spark';

export interface Particle {
  mode: ParticleMode;
  color: string;
  /** Start offset from the anchor point. */
  x: number;
  y: number;
  /** Velocity and acceleration per particle lifetime. */
  vx: number;
  vy: number;
  ax: number;
  ay: number;
  /** When the particle appears and how long it lives, as fractions of the animation. */
  delay: number;
  life: number;
  size: number;
  sizeEnd: number;
  /** Circles the anchor instead of flying: start angle, turns over its life, start radius (shrinks to 0). */
  orbit: { angle: number; turns: number; radius: number } | null;
}

/** A deterministic random source, so an effect looks the same each time a cue plays. */
export type Random = () => number;

/** A seeded PRNG (mulberry32). */
export function seededRandom(seed: number): Random {
  let state = seed >>> 0;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

/** A stable 32-bit hash of a string, to seed {@link seededRandom}. */
export function hashSeed(text: string): number {
  let hash = 2166136261;
  for (let i = 0; i < text.length; i++) {
    hash = Math.imul(hash ^ text.charCodeAt(i), 16777619);
  }
  return hash >>> 0;
}

/** A value in `[min, max)`. */
export function between(random: Random, [min, max]: readonly [number, number]): number {
  return min + random() * (max - min);
}

/** How to spawn a burst of particles. Ranges are `[min, max]`. */
export interface BurstOptions {
  count: number;
  mode: ParticleMode;
  colors: readonly string[];
  /** Launch speed and direction (radians; 0 = right, -π/2 = up), and how wide the spray is. */
  speed: readonly [number, number];
  direction?: number;
  spread?: number;
  /** Constant acceleration (negative `y` rises, like heat). */
  gravity?: { x: number; y: number };
  size: readonly [number, number];
  /** End size as a factor of the start size. */
  grow?: number;
  life: readonly [number, number];
  delay?: readonly [number, number];
  /** Half-width and half-height of the box particles start in. */
  area?: { x: number; y: number };
}

/** Spawns a burst of particles around the anchor point. */
export function burst(random: Random, options: BurstOptions): Particle[] {
  const direction = options.direction ?? 0;
  const spread = options.spread ?? Math.PI * 2;
  const area = options.area ?? { x: 0, y: 0 };
  return Array.from({ length: options.count }, () => {
    const angle = direction + (random() - 0.5) * spread;
    const speed = between(random, options.speed);
    const size = between(random, options.size);
    return {
      mode: options.mode,
      color: options.colors[Math.floor(random() * options.colors.length)],
      x: (random() * 2 - 1) * area.x,
      y: (random() * 2 - 1) * area.y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      ax: options.gravity?.x ?? 0,
      ay: options.gravity?.y ?? 0,
      delay: between(random, options.delay ?? [0, 0]),
      life: between(random, options.life),
      size,
      sizeEnd: size * (options.grow ?? 1),
      orbit: null
    };
  });
}

/** Spawns particles that spiral into the anchor point. */
export function swirl(random: Random, options: Pick<BurstOptions, 'count' | 'mode' | 'colors' | 'size' | 'life' | 'delay'>
  & { radius: readonly [number, number]; turns: number }): Particle[] {
  return Array.from({ length: options.count }, (_, i) => {
    const size = between(random, options.size);
    return {
      mode: options.mode,
      color: options.colors[i % options.colors.length],
      x: 0, y: 0, vx: 0, vy: 0, ax: 0, ay: 0,
      delay: between(random, options.delay ?? [0, 0]),
      life: between(random, options.life),
      size,
      sizeEnd: size * 0.4,
      orbit: { angle: random() * Math.PI * 2, turns: options.turns, radius: between(random, options.radius) }
    };
  });
}

/**
 * Draws the particles as they are at `progress` (0–1 over the animation), around the anchor
 * point `(cx, cy)` with anchor size `unit` in pixels.
 */
export function drawParticles(draw: CanvasRenderingContext2D, particles: readonly Particle[], progress: number,
                              cx: number, cy: number, unit: number): void {
  for (const p of particles) {
    const age = (progress - p.delay) / p.life;
    if (age < 0 || age > 1) continue;
    const alpha = Math.min(1, age * 6) * (1 - age) ** 1.4;
    const size = Math.max(0.5, (p.size + (p.sizeEnd - p.size) * age) * unit);
    let x: number;
    let y: number;
    if (p.orbit) {
      const angle = p.orbit.angle + p.orbit.turns * Math.PI * 2 * age;
      const radius = p.orbit.radius * (1 - age) * unit;
      x = cx + Math.cos(angle) * radius;
      y = cy + Math.sin(angle) * radius * 0.8;
    } else {
      x = cx + (p.x + p.vx * age + 0.5 * p.ax * age * age) * unit;
      y = cy + (p.y + p.vy * age + 0.5 * p.ay * age * age) * unit;
    }
    draw.globalAlpha = alpha;
    if (p.mode === 'spark') {
      const vx = p.vx + p.ax * age;
      const vy = p.vy + p.ay * age;
      const speed = Math.hypot(vx, vy) || 1;
      const length = size * 3;
      draw.strokeStyle = p.color;
      draw.lineWidth = Math.max(1, size * 0.5);
      draw.lineCap = 'round';
      draw.beginPath();
      draw.moveTo(x - (vx / speed) * length, y - (vy / speed) * length);
      draw.lineTo(x, y);
      draw.stroke();
    } else {
      draw.fillStyle = softDot(draw, x, y, size, p.color);
      draw.beginPath();
      draw.arc(x, y, size, 0, Math.PI * 2);
      draw.fill();
    }
  }
  draw.globalAlpha = 1;
}

/** A radial gradient from `color` at the centre to transparent at `radius`. */
export function softDot(draw: CanvasRenderingContext2D, x: number, y: number, radius: number, color: string): CanvasGradient {
  const gradient = draw.createRadialGradient(x, y, 0, x, y, radius);
  gradient.addColorStop(0, color);
  gradient.addColorStop(1, transparent(color));
  return gradient;
}

/** The same `rgb(…)`/`rgba(…)` color at alpha 0. */
export function transparent(color: string): string {
  const match = /^rgba?\(([^,]+),([^,]+),([^,)]+)/.exec(color.replace(/\s/g, ''));
  return match ? `rgba(${match[1]},${match[2]},${match[3]},0)` : 'rgba(0,0,0,0)';
}
