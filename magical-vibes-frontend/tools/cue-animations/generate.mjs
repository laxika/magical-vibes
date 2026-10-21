/**
 * Generates the visual cue Lottie animations in `public/cue-animations/` (run
 * `npm run cue-animations` from `magical-vibes-frontend/`). Every animation is built from code
 * here, so it is ours to license, its colors can be palette slots, and a change is one edit and a
 * re-run instead of a hand-edited JSON file. See `public/cue-animations/README.md`.
 */
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  compact, composition, ellipse, fill, group, keys, layer, path, rect, repeater, star, stroke, trim
} from './lottie.mjs';

const OUT = join(dirname(fileURLToPath(import.meta.url)), '../../public/cue-animations');

/** Outlines, so pale looks still read on the light board. */
const INK = '#1b1022';
const WHITE = '#ffffff';
const BONE = '#ece6f2';
const SOCKET = '#140a1a';

/** A deterministic pseudo-random sequence, so re-running the generator gives the same files. */
function seeded(seed) {
  let state = seed >>> 0;
  return () => {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const polar = (angle, radius) => [Math.cos(angle) * radius, Math.sin(angle) * radius];
const degrees = angle => angle * Math.PI / 180;

/** A colored stroke over a slightly wider, faint dark one, so it shows on light and dark cards. */
function outlined(color, width, darkOpacity = 35) {
  return [stroke(color, width), stroke(INK, width + 2.5, { opacity: darkOpacity })];
}

/** Opacity that is fully on from `on` to `off`, then fades out by `end`. */
function fadeOut(on, off, end) {
  return keys([on, 100, 'hold'], [off, 100], [end, 0]);
}

/** A jagged line from `from` towards `angle`, for cracks and lightning. */
function jagged(random, from, angle, length, segments, jitter) {
  const points = [from];
  for (let i = 1; i <= segments; i++) {
    const along = polar(angle, length * i / segments);
    const across = polar(angle + Math.PI / 2, (random() * 2 - 1) * jitter * (i === segments ? 0.3 : 1));
    points.push([from[0] + along[0] + across[0], from[1] + along[1] + across[1]]);
  }
  return points;
}

/** Damage: a white core flash, a starburst in the look's color, a shock ring and flying shards. */
function impact() {
  return composition('impact', { frames: 27 }, [
    layer('core', [group('core', [star(8, 34, 12), fill(WHITE)])], {
      op: 15, s: keys([0, [20, 20], 'out'], [6, [100, 100], 'in'], [14, [0, 0]]), r: keys([0, 0, 'out'], [14, 30])
    }),
    layer('burst', [group('burst', [star(8, 72, 24), fill('@c1'), stroke(INK, 3, { opacity: 55 })])], {
      op: 21, s: keys([0, [15, 15], 'out'], [8, [100, 100], 'in'], [20, [55, 55]]), r: keys([0, -12, 'out'], [20, 18]),
      o: fadeOut(0, 9, 20)
    }),
    layer('ring', [group('ring', [ellipse(100), ...outlined('@accent', keys([0, 9, 'out'], [22, 1.5]), 30)])], {
      s: keys([0, [20, 20], 'out'], [22, [175, 175]]), o: keys([0, 100], [22, 0])
    }),
    layer('shards', [group('shards', [
      group('shard', [star(4, 11, null), fill('@accent'), stroke(INK, 1.5, { opacity: 45 })], {
        p: keys([0, [0, -28], 'out'], [22, [0, -92]]), s: [55, 150]
      }),
      repeater(6, { r: 60 })
    ], { r: 30 })], { o: fadeOut(0, 12, 24) })
  ]);
}

/** A magic circle that draws itself: two rings, an eight-pointed figure and runes, turning as it fades. */
function sigil() {
  const drawn = (from, to) => trim(0, keys([from, 0, 'out'], [to, 100]));
  return composition('sigil', { frames: 30 }, [
    layer('sigil', [
      group('runes', [
        group('rune', [star(4, 8, 2.6), fill('@accent'), stroke(INK, 1.2, { opacity: 40 })], {
          p: [0, -76], s: keys([8, [0, 0], 'back'], [14, [100, 100]])
        }),
        repeater(8, { r: 45 })
      ]),
      group('octagram', [star(4, 50, null), star(4, 50, null, { r: 45 }), drawn(4, 16), ...outlined('@accent', 3)]),
      group('inner ring', [ellipse(94), trim(keys([2, 100, 'out'], [14, 0]), 100), ...outlined('@c1', 2.5, 25)]),
      group('outer ring', [ellipse(128), drawn(0, 12), ...outlined('@accent', 4)]),
      group('glow', [ellipse(136), fill('@c0', 12)])
    ], {
      o: keys([0, 0, 'out'], [4, 100, 'hold'], [19, 100], [30, 0]),
      s: keys([0, [82, 82], 'out'], [30, [108, 108]]),
      r: keys([0, -30, 'out'], [30, 30])
    })
  ]);
}

/** A countered spell: a rune disc cracks, then breaks into six shards that fly apart. */
function shatter() {
  const shards = Array.from({ length: 6 }, (_, i) => {
    const from = degrees(i * 60 - 90);
    const to = degrees(i * 60 - 30);
    const middle = (from + to) / 2;
    const [dx, dy] = polar(middle, 72);
    return layer('shard ' + i, [group('shard', [
      path([[0, 0], polar(from, 40), polar(middle, 42), polar(to, 40)]),
      fill('@c1', 90), stroke('@accent', 2.5), stroke(INK, 4.5, { opacity: 35 })
    ])], {
      ip: 11, p: keys([11, [0, 0], 'out'], [29, [dx, dy + 18]]), r: keys([11, 0, 'out'], [29, i % 2 ? 50 : -50]),
      o: fadeOut(11, 19, 29)
    });
  });
  return composition('shatter', { width: 260, height: 260, frames: 30 }, [
    layer('crack', [group('crack', [
      path([[-30, -26], [-9, -7], [-15, 5], [6, 13], [1, 24], [28, 31]], { closed: false }),
      trim(0, keys([5, 0, 'out'], [10, 100])), stroke(WHITE, 3), stroke(INK, 5.5, { opacity: 50 })
    ])], { ip: 5, op: 12 }),
    layer('disc', [
      group('rune', [star(4, 22, 7), stroke('@accent', 2.5)]),
      group('disc', [ellipse(80), fill('@c1', 85), stroke('@accent', 4), stroke(INK, 7, { opacity: 40 })])
    ], { op: 12, s: keys([0, [30, 30], 'back'], [7, [100, 100]]) }),
    layer('flash', [group('flash', [ellipse(70), ...outlined('@accent', keys([11, 8, 'out'], [24, 1]), 30)])], {
      ip: 11, s: keys([11, [60, 60], 'out'], [24, [190, 190]]), o: keys([11, 100], [24, 0])
    }),
    ...shards
  ]);
}

/** A destroyed permanent: dark cracks race across the card from a flash in its middle, then fade. */
function crack() {
  const random = seeded(7);
  const lines = [-100, -35, 18, 88, 150, 215].map((angle, i) =>
    path(jagged(random, [0, 0], degrees(angle), 62 + random() * 22, 5, 9), { closed: false }));
  const branches = [[-35, 4], [150, 8], [88, 2]].map(([angle, offset]) => {
    const [x, y] = polar(degrees(angle), 30 + offset);
    return path(jagged(random, [x, y], degrees(angle + 40), 26, 3, 5), { closed: false });
  });
  return composition('crack', { width: 200, height: 220, frames: 36 }, [
    layer('flash', [group('flash', [star(6, 28, 9), fill('@accent'), stroke(INK, 2, { opacity: 50 })])], {
      op: 14, s: keys([0, [0, 0], 'out'], [5, [100, 100], 'in'], [13, [0, 0]]), r: keys([0, 0], [13, 40])
    }),
    layer('cracks', [group('cracks', [
      ...lines, ...branches, trim(0, keys([0, 0, 'out'], [12, 100])),
      stroke('@accent', 2), stroke(INK, 5.5, { opacity: 85 })
    ])], { o: fadeOut(0, 24, 36) })
  ]);
}

/** An exiled permanent: a column of light flares up through it while a ring closes in and sparkles rise. */
function pillar() {
  const beam = (width, color, opacity) => group('beam ' + width, [ellipse([width, 320]), fill(color, opacity)]);
  const sparkles = [[-26, 0], [20, 5], [-6, 10], [30, 14], [-34, 18]].map(([x, delay], i) =>
    layer('sparkle ' + i, [group('sparkle', [star(4, 9, 2.8), fill(WHITE), stroke(INK, 1.4, { opacity: 40 })])], {
      ip: delay, p: keys([delay, [x, 50], 'out'], [delay + 20, [x * 0.8, -130]]), o: fadeOut(delay, delay + 12, delay + 20),
      r: keys([delay, 0], [delay + 20, 90])
    }));
  return composition('pillar', { width: 200, height: 320, frames: 36 }, [
    ...sparkles,
    layer('ring', [group('ring', [ellipse([180, 56]), ...outlined('@accent', 4)])], {
      p: [0, 60], s: keys([0, [100, 100], 'in'], [30, [10, 10]]), o: keys([0, 0, 'out'], [5, 100, 'hold'], [22, 100], [30, 0])
    }),
    layer('beam', [beam(20, WHITE, 95), beam(40, '@c0', 70), beam(64, '@c1', 45), beam(92, '@c2', 25)], {
      s: keys([0, [4, 100], 'out'], [8, [100, 100], 'hold'], [22, [100, 100], 'inOut'], [34, [0, 100]]),
      o: keys([0, 0, 'out'], [4, 100, 'hold'], [28, 100], [36, 0])
    })
  ]);
}

/** A bounced permanent: three spiral arms whirl inwards and shrink into the middle. */
function vortex() {
  const arm = Array.from({ length: 15 }, (_, i) => polar(degrees(i * 22), 78 - i * 5));
  return composition('vortex', { frames: 36 }, [
    layer('core', [group('core', [ellipse(46), fill('@c0', 70), stroke('@accent', 2.5)])], {
      s: keys([0, [0, 0], 'out'], [14, [100, 100], 'in'], [34, [0, 0]])
    }),
    layer('arms', [group('arms', [
      group('arm', [path(arm, { closed: false, smooth: 0.9 }),
        trim(keys([6, 0, 'in'], [34, 100]), keys([0, 12, 'out'], [16, 100])),
        stroke('@c1', keys([0, 8, 'linear'], [36, 2.5])), stroke(INK, keys([0, 11, 'linear'], [36, 5]), { opacity: 30 })]),
      repeater(3, { r: 120 })
    ])], {
      r: keys([0, 0, 'in'], [36, 420]), s: keys([0, [100, 100], 'linear'], [36, [40, 40]]),
      o: keys([0, 0, 'out'], [5, 100, 'hold'], [28, 100], [36, 0])
    })
  ]);
}

/** A death: pale wisps leave the card and drift up, wobbling as they fade. */
function wisp() {
  const shape = path([[0, -24], [8, -7], [11, 6], [0, 15], [-11, 6], [-8, -7]], { smooth: 0.9 });
  const wisps = [[-24, 0, 14], [20, 5, -12], [-2, 9, 8]].map(([x, delay, wobble], i) =>
    layer('wisp ' + i, [
      group('eye', [ellipse([4, 6], [-3.5, 2]), ellipse([4, 6], [3.5, 2]), fill(INK, 55)]),
      group('wisp', [shape, fill(WHITE, 80), stroke('@c1', 2.5), stroke(INK, 4.5, { opacity: 25 })])
    ], {
      ip: delay,
      p: keys([delay, [x, 30], 'out'], [delay + 14, [x + wobble, -36]], [delay + 27, [x - wobble * 0.5, -110]]),
      s: keys([delay, [40, 40], 'out'], [delay + 9, [100, 100]], [delay + 27, [70, 90]]),
      r: keys([delay, 0], [delay + 14, wobble], [delay + 27, -wobble * 0.5]),
      o: keys([delay, 0, 'out'], [delay + 5, 100, 'hold'], [delay + 16, 100], [delay + 27, 0])
    }));
  return composition('wisp', { width: 200, height: 280, frames: 36 }, wisps);
}

/** Life gained: a glowing cross pops up and rises inside a widening ring, with small crosses floating up. */
function lifeGain() {
  const cross = (long, short, round) => [rect([short, long], { r: round }), rect([long, short], { r: round })];
  const sparkles = [[-36, 6], [32, 9], [-12, 13], [40, 16]].map(([x, delay], i) =>
    layer('sparkle ' + i, [group('sparkle', [...cross(14, 5, 2), fill('@c1'), stroke(INK, 1.4, { opacity: 40 })])], {
      ip: delay, p: keys([delay, [x, 30], 'out'], [delay + 17, [x, -60]]), o: fadeOut(delay, delay + 9, delay + 17)
    }));
  return composition('life-gain', { frames: 33 }, [
    ...sparkles,
    layer('cross', [
      group('core', [...cross(40, 8, 3), fill(WHITE, 85)]),
      group('cross', [...cross(58, 20, 6), fill('@c1'), stroke(INK, 3, { opacity: 45 })])
    ], {
      s: keys([0, [0, 0], 'back'], [10, [100, 100], 'hold'], [20, [100, 100], 'in'], [33, [70, 70]]),
      p: keys([0, [0, 10], 'out'], [33, [0, -36]]), o: fadeOut(0, 20, 33)
    }),
    layer('ring', [group('ring', [ellipse(60), ...outlined('@accent', keys([0, 5, 'out'], [24, 1.5]), 30)])], {
      s: keys([0, [40, 40], 'out'], [24, [210, 210]]), o: keys([0, 100], [24, 0])
    })
  ]);
}

/** Life lost: a drop falls, squashes, and splashes into a ring and flying droplets. */
function lifeLoss() {
  const droplets = [-34, -18, -6, 8, 20, 36].map((dx, i) =>
    layer('droplet ' + i, [group('droplet', [ellipse(i % 2 ? 7 : 9), fill('@c2'), stroke(INK, 1.5, { opacity: 45 })])], {
      ip: 12, p: keys([12, [0, 4], 'out'], [20, [dx, -22 - (i % 3) * 8], 'in'], [31, [dx * 1.5, 22]]),
      o: fadeOut(12, 22, 31)
    }));
  return composition('life-loss', { frames: 33 }, [
    layer('drop', [
      group('shine', [ellipse([5, 9], [-4, 3]), fill(WHITE, 60)]),
      group('drop', [path([[0, -26], [12, -2], [12, 10], [0, 20], [-12, 10], [-12, -2]], { smooth: 0.8 }),
        fill('@c2'), stroke(INK, 2.5, { opacity: 60 })])
    ], {
      op: 14, p: keys([0, [0, -62], 'in'], [12, [0, 0]]),
      s: keys([0, [60, 60], 'out'], [10, [100, 112]], [13, [150, 55]]), o: keys([0, 0, 'out'], [3, 100, 'hold'], [12, 100], [14, 0])
    }),
    ...droplets,
    layer('splash', [group('splash', [ellipse([80, 28]), ...outlined('@c2', 4, 40)])], {
      ip: 12, p: [0, 12], s: keys([12, [20, 20], 'out'], [27, [130, 130]]), o: keys([12, 70], [27, 0])
    })
  ]);
}

/** Counters: a round badge pops in with a ring of orbiting dots, then shrinks away. */
function badge() {
  return composition('badge', { frames: 33 }, [
    layer('badge', [
      group('shine', [ellipse(16, [-14, -14]), fill(WHITE, 55)]),
      group('inner ring', [ellipse(50), stroke('@c0', 3)]),
      group('badge', [ellipse(66), fill('@c1'), stroke(INK, 4, { opacity: 60 })])
    ], { s: keys([0, [0, 0], 'back'], [9, [100, 100], 'hold'], [22, [100, 100], 'in'], [33, [0, 0]]) }),
    layer('orbit', [group('orbit', [
      group('dot', [ellipse(7), fill('@accent'), stroke(INK, 1.5, { opacity: 40 })], { p: [0, -50] }),
      repeater(10, { r: 36 })
    ])], {
      r: keys([0, 0, 'out'], [33, 120]), s: keys([0, [60, 60], 'out'], [12, [112, 112]]),
      o: keys([0, 0, 'out'], [4, 100, 'hold'], [20, 100], [33, 0])
    })
  ]);
}

/** Doom Blade: a dark cloud billows in from nothing, churns, and flickers with violet lightning. */
function doomCloud() {
  const random = seeded(31);
  const puffs = [
    [0, -22, 86, 0], [-38, -12, 72, 3], [40, -10, 74, 2], [-72, 10, 58, 6], [74, 12, 56, 7],
    [-30, 22, 72, 4], [28, 22, 74, 5], [0, 26, 64, 8]
  ];
  const flicker = (from, x, angle) => layer('lightning ' + from, [group('bolt', [
    path(jagged(random, [x, 18], degrees(angle), 58, 5, 9), { closed: false }), stroke(WHITE, 2), stroke('@accent', 5)
  ])], { ip: from, op: from + 5 });
  return composition('doom-cloud', { width: 260, height: 160, frames: 78 }, [
    flicker(30, -18, 100), flicker(52, 26, 75),
    ...puffs.map(([x, y, size, delay], i) => layer('puff ' + i, [
      group('rim', [ellipse(size * 0.8, [0, -size * 0.12]), fill('@c1')]),
      group('puff', [ellipse(size), fill('@c0'), stroke('@accent', 2.5, { opacity: 35 })])
    ], {
      ip: delay,
      p: keys([delay, [x * 0.4, y + 10], 'out'], [delay + 18, [x, y], 'linear'], [78, [x + (random() - 0.5) * 14, y + (random() - 0.5) * 8]]),
      s: keys([delay, [0, 0], 'out'], [delay + 18, [100, 100], 'inOut'], [48, [106, 96], 'inOut'], [78, [98, 104]])
    })).reverse()
  ]);
}

/** Doom Blade: a skull forms in the cloud and its eyes ignite. The effect moves it and fades it. */
function skull() {
  const cranium = path([
    [0, -40], [27, -34], [38, -13], [36, 8], [25, 19], [22, 31], [-22, 31], [-25, 19], [-36, 8], [-38, -13], [-27, -34]
  ], { smooth: 0.75 });
  const eyes = [[-14, 7], [14, 7]];
  const socket = side => path([[-25, -3], [-5, 3], [-7, 13], [-15, 15], [-24, 9]]
    .map(([x, y]) => [x * side, y]), { smooth: 0.6 });
  return composition('skull', { frames: 40 }, [
    layer('skull', [
      group('eyes', [...eyes.map(([x, y]) => ellipse(8, [x, y])), fill('@accent'),
        ...eyes.map(([x, y]) => ellipse(20, [x, y])), fill('@accent', 40)], {
        s: keys([12, [0, 0], 'back'], [20, [100, 100], 'inOut'], [30, [80, 80], 'inOut'], [40, [105, 105]])
      }),
      group('teeth', [
        ...[-10, -3.3, 3.3, 10].map(x => path([[x, 30], [x, 41]], { closed: false })), stroke(SOCKET, 2.2)
      ]),
      group('sockets', [socket(1), socket(-1), path([[0, 14], [-5, 23], [5, 23]]), fill(SOCKET)]),
      group('jaw', [rect([36, 16], { p: [0, 36], r: 5 }), fill(BONE), stroke(INK, 3)]),
      group('cranium', [cranium, fill(BONE), stroke(INK, 3)]),
      group('halo', [ellipse(92), fill('@accent', 30), ellipse(124), fill('@accent', 20)])
    ], { s: keys([0, [40, 40], 'back'], [14, [100, 100]]), o: keys([0, 0, 'out'], [8, 100]) })
  ]);
}

const ANIMATIONS = {
  impact, sigil, shatter, crack, pillar, vortex, wisp, 'life-gain': lifeGain, 'life-loss': lifeLoss, badge,
  'doom-cloud': doomCloud, skull
};

mkdirSync(OUT, { recursive: true });
for (const [name, build] of Object.entries(ANIMATIONS)) {
  writeFileSync(join(OUT, name + '.json'), JSON.stringify(compact(build())) + '\n');
}
console.log(`Wrote ${Object.keys(ANIMATIONS).length} animations to ${OUT}`);
