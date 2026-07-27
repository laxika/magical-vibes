import { Permanent } from '../../services/websocket.service';
import { IndexedPermanent, LandStack, isLandStack } from './battlefield.utils';

/** One player's half of the battlefield, as the fit model sees it. */
export interface BattlefieldSide {
  creatures: IndexedPermanent[];
  lands: (IndexedPermanent | LandStack)[];
  /** The side holds no permanents at all, so it renders the "No permanents" slot instead of rows. */
  isEmpty: boolean;
  /** Extra full-height rows below the permanents (revealed hand, revealed library top, playable exile). */
  revealedRows: number;
}

/** The battlefield area's flex-allocated size, as measured by a ResizeObserver. */
export interface BattlefieldAreaSize {
  width: number;
  height: number;
}

/** Cards tucked under a permanent (attached auras, cards exiled with it) that peek out past it. */
export type TuckedCount = (perm: Permanent) => number;

/**
 * The board's fit model: how big a board would render at a given zoom, and therefore the
 * largest zoom at which it still fits its allocation. Shared by the real game and the
 * tutorial so both render cards at the same size for the same board.
 *
 * Every constant here mirrors a dimension in shared-game-styles.css / card-display.component.css;
 * changing card sizes, row paddings or stack overlaps there means changing the matching constant
 * here, or the fit drifts.
 */
export class BattlefieldFitModel {
  /* Dimensions mirrored from shared-game-styles.css, used to fit both
     battlefields into the area's flex-allocated size. */
  static readonly CARD_HEIGHT = 231;
  static readonly CARD_WIDTH = 165;
  /* A tapped card reserves its full rotated footprint (full size, neighbours
     shift aside — overlapping or shrinking tapped cards were both rejected).
     Widths are therefore tap-accurate: tapping can re-wrap a crowded row and,
     rarely, change the side's zoom — the accepted tradeoff. */
  static readonly TAPPED_CARD_WIDTH = 231;
  /* Box offset between stacked basic lands: the visible strip of each land. */
  static readonly STACK_STRIP = 32;
  /* Vertical step between stacked basic lands (MTGO-style diagonal fan);
     matches the horizontal STACK_STRIP. */
  static readonly LAND_STACK_Y_STEP = 32;
  static readonly ROW_GAP = 10;
  /* Attached auras peek out from under their host: 50px to the side
     (margin-left) and 41px above (231px card minus the -190px overlap). */
  static readonly AURA_X_OFFSET = 50;
  static readonly AURA_STRIP = 41;
  static readonly LANDS_ROW_MODIFIER = 0.9;
  static readonly SUB_ROW_PADDING = 8;
  static readonly SIDE_LABEL_HEIGHT = 0;
  /* CSS zoom rounds each scaled line up to whole pixels, so a modeled line runs
     a few px short of what renders; this per-line and global slack keeps the
     model a hair conservative so a tight fit shrinks instead of hairline-scrolling. */
  static readonly LINE_SLACK = 3;
  static readonly FIT_SAFETY = 12;
  static readonly ROW_MARGIN = 0;
  static readonly EMPTY_MESSAGE_HEIGHT = 20;
  static readonly REVEALED_ROW_HEIGHT = 250;
  /* The divider between the two halves; grows to a visible red line during combat. */
  static readonly DIVIDER_HEIGHT = 3;
  /* Clear strip between each side and the divider (the divider's CSS margin)
     that attacking/blocking creatures advance into via the ±30px combat nudge,
     so combat cards never overlap the opposing row. */
  static readonly COMBAT_CORRIDOR = 30;
  /* Low floor so an unbalanced board (one side's content is much taller than
     half) scales its cards down to stay inside its half instead of scrolling. */
  static readonly MIN_BATTLEFIELD_ZOOM = 0.3;
  /* Cards never render above 80% of natural size; they only shrink further to fit. */
  static readonly MAX_BATTLEFIELD_ZOOM = 0.8;

  /** Boards without auras or exiled-with cards (the tutorial's) need no tucked-card hook. */
  constructor(private readonly tuckedCount: TuckedCount = () => 0) {}

  /** Greedy flex-wrap simulation: how many lines the given item widths need. */
  private static packedLines(widths: number[], gap: number, rowWidth: number): number {
    let lines = 1;
    let x = 0;
    for (const w of widths) {
      const next = x === 0 ? w : x + gap + w;
      if (next > rowWidth && x > 0) {
        lines++;
        x = w;
      } else {
        x = next;
      }
    }
    return lines;
  }

  /** Footprint width (zoom 1) of a permanent plus any attached auras or exiled-with cards. */
  private stackWidth(perm: Permanent): number {
    const C = BattlefieldFitModel;
    const base = perm.tapped ? C.TAPPED_CARD_WIDTH : C.CARD_WIDTH;
    return base + (this.tuckedCount(perm) > 0 ? C.AURA_X_OFFSET : 0);
  }

  /** Reserved footprint height (zoom 1) of a permanent plus any attached auras.
      Tap state is intentionally ignored: a tapped card renders shorter (rotated,
      165px), but if the modeled height shrank on tap the whole side's cards would
      rescale every time a land is tapped for mana or a creature attacks. Reserving
      the upright height keeps the per-side zoom stable across tap/untap — a tapped
      card just leaves a little unused vertical space, never an overflow. */
  private stackHeight(perm: Permanent): number {
    const C = BattlefieldFitModel;
    return C.CARD_HEIGHT + this.tuckedCount(perm) * C.AURA_STRIP;
  }

  /** Height of one player's battlefield (creatures row + lands row + revealed rows)
      at the given zoom, including horizontal wrapping of crowded rows. */
  sideHeight(side: BattlefieldSide, zoom: number, rowWidth: number): number {
    const C = BattlefieldFitModel;
    const rowHeight = (widths: number[], lineHeight: number): number => {
      if (widths.length === 0) return 0;
      const lines = C.packedLines(widths, C.ROW_GAP, rowWidth);
      return lines * (Math.ceil(lineHeight) + C.LINE_SLACK) + (lines - 1) * C.ROW_GAP + C.SUB_ROW_PADDING;
    };
    /* Auras and cards exiled with a permanent peek out past their host, and a stack
       holding any is that much bigger. Reserved for the stack as a whole rather than per
       member: over-reserving keeps the fit conservative, and it makes a one-land stack
       measure exactly like the bare land it replaced, which is what stackBasicLands now
       emits for a single basic. */
    const landItemWidth = (item: IndexedPermanent | LandStack, landZoom: number): number => {
      if (isLandStack(item)) {
        /* Each land after the first advances by its predecessor's visible
           strip, so the stack ends at the LAST land's box: strips + that
           land's (tap-dependent) width. Mirrors the land-stack CSS margins. */
        const last = item.lands[item.lands.length - 1].perm;
        const lastWidth = last.tapped ? C.TAPPED_CARD_WIDTH : C.CARD_WIDTH;
        const tucked = item.lands.some(ip => this.tuckedCount(ip.perm) > 0) ? C.AURA_X_OFFSET : 0;
        return ((item.lands.length - 1) * C.STACK_STRIP + lastWidth + tucked) * landZoom;
      }
      return this.stackWidth(item.perm) * landZoom;
    };
    /* Reserve the upright line height regardless of tap state so the lands row
       (and thus the side's zoom) doesn't jump when lands tap/untap; see stackHeight. */
    const landItemHeight = (item: IndexedPermanent | LandStack): number => {
      if (isLandStack(item)) {
        /* Each land after the first steps down by LAND_STACK_Y_STEP, so the
           stack is one card plus the accumulated vertical fan. */
        const tucked = Math.max(0, ...item.lands.map(ip => this.tuckedCount(ip.perm)));
        return C.CARD_HEIGHT + (item.lands.length - 1) * C.LAND_STACK_Y_STEP + tucked * C.AURA_STRIP;
      }
      return this.stackHeight(item.perm);
    };

    let h = C.SIDE_LABEL_HEIGHT + C.ROW_MARGIN + side.revealedRows * C.REVEALED_ROW_HEIGHT;
    if (side.isEmpty) {
      return h + C.EMPTY_MESSAGE_HEIGHT;
    }
    const creatureLine = side.creatures.length > 0
      ? Math.max(...side.creatures.map(ip => this.stackHeight(ip.perm)))
      : 0;
    h += rowHeight(side.creatures.map(ip => this.stackWidth(ip.perm) * zoom), creatureLine * zoom);
    const landZoom = zoom * C.LANDS_ROW_MODIFIER;
    const landLine = side.lands.length > 0 ? Math.max(...side.lands.map(landItemHeight)) : 0;
    h += rowHeight(side.lands.map(item => landItemWidth(item, landZoom)), landLine * landZoom);
    return h;
  }

  /** Largest zoom (MAX→MIN) at which one side's content fits in its half of the
      area. Each half = (area - safety - divider - corridors) / 2, matching the
      flex layout. */
  sideZoom(side: BattlefieldSide, area: BattlefieldAreaSize): number {
    const C = BattlefieldFitModel;
    if (!area.width || !area.height) return 1;
    const budget = (area.height - C.FIT_SAFETY - C.DIVIDER_HEIGHT) / 2 - C.COMBAT_CORRIDOR;
    return C.largestZoomFitting(z => this.sideHeight(side, z, area.width) <= budget);
  }

  /** Board-wide zoom, kept as the battlefield area's fallback density. The two players are
      flex halves, so the board fits when the taller side fits into its half; the divider (red
      during combat) sits between them. Combat doesn't add its own space — the attacking/blocking
      creatures stay in their rows and merely nudge toward the divider — so the fit is the same
      in and out of combat. */
  boardZoom(sides: BattlefieldSide[], area: BattlefieldAreaSize): number {
    const C = BattlefieldFitModel;
    if (!area.width || !area.height) return 1;
    const budget = area.height - C.FIT_SAFETY;
    const modeledBoardHeight = (zoom: number): number =>
      2 * Math.max(...sides.map(side => this.sideHeight(side, zoom, area.width)))
      + C.DIVIDER_HEIGHT + 2 * C.COMBAT_CORRIDOR;
    return C.largestZoomFitting(z => modeledBoardHeight(z) <= budget);
  }

  private static largestZoomFitting(fits: (zoom: number) => boolean): number {
    const C = BattlefieldFitModel;
    for (let z = C.MAX_BATTLEFIELD_ZOOM; z > C.MIN_BATTLEFIELD_ZOOM; z -= 0.02) {
      if (fits(z)) {
        return Math.round(z * 100) / 100;
      }
    }
    return C.MIN_BATTLEFIELD_ZOOM;
  }

  /** The hand is never fit-modeled — it just gets tighter once it holds more than a screenful. */
  static handZoom(handSize: number): number {
    return handSize > 9 ? 0.6 : 0.68;
  }
}
