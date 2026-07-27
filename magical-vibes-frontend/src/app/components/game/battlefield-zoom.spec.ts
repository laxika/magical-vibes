import { Card, Permanent } from '../../services/websocket.service';
import { IndexedPermanent, LandStack } from './battlefield.utils';
import { BattlefieldFitModel, BattlefieldSide } from './battlefield-zoom';

/** Minimal Permanent factory for the fit-model tests. */
function perm(id: string, tapped = false): Permanent {
  return {
    id,
    tapped,
    attachedTo: null,
    animatedCreature: false,
    grantedKeywords: [],
    removedKeywords: [],
    card: { id, name: id, type: 'CREATURE', additionalTypes: [], supertypes: [], subtypes: [], keywords: [] } as unknown as Card,
  } as unknown as Permanent;
}

function indexed(id: string, tapped = false): IndexedPermanent {
  return { perm: perm(id, tapped), originalIndex: 0 };
}

function landStack(ids: string[]): LandStack {
  return { key: ids[0], lands: ids.map(id => indexed(id)) } as unknown as LandStack;
}

function side(over: Partial<BattlefieldSide> = {}): BattlefieldSide {
  return { creatures: [], lands: [], isEmpty: false, revealedRows: 0, ...over };
}

/** Roughly a 1600x900 desktop board area. */
const AREA = { width: 1240, height: 620 };

describe('BattlefieldFitModel', () => {
  const model = new BattlefieldFitModel();

  it('renders a sparse side at the ceiling zoom rather than blowing cards up to fill the half', () => {
    const zoom = model.sideZoom(side({ creatures: [indexed('bears')] }), AREA);

    expect(zoom).toBe(BattlefieldFitModel.MAX_BATTLEFIELD_ZOOM);
  });

  it('shrinks a side as it fills up, never past the floor', () => {
    const wide = side({ creatures: Array.from({ length: 60 }, (_, i) => indexed(`c${i}`)) });

    const zoom = model.sideZoom(wide, AREA);

    expect(zoom).toBeLessThan(BattlefieldFitModel.MAX_BATTLEFIELD_ZOOM);
    expect(zoom).toBeGreaterThanOrEqual(BattlefieldFitModel.MIN_BATTLEFIELD_ZOOM);
  });

  it('sizes each side only from its own content, so a full opponent never resizes our cards', () => {
    const mine = side({ creatures: [indexed('bears')] });
    const theirs = side({ creatures: Array.from({ length: 40 }, (_, i) => indexed(`c${i}`)) });

    expect(model.sideZoom(mine, AREA)).toBeGreaterThan(model.sideZoom(theirs, AREA));
  });

  it('reserves the upright height for a tapped permanent, so tapping for mana never rescales the row', () => {
    const untapped = side({ creatures: Array.from({ length: 3 }, (_, i) => indexed(`c${i}`)) });
    const tapped = side({ creatures: Array.from({ length: 3 }, (_, i) => indexed(`c${i}`, true)) });

    expect(model.sideZoom(tapped, AREA)).toBe(model.sideZoom(untapped, AREA));
  });

  /* The accepted tradeoff of tap-accurate widths: a tapped card reserves its rotated
     footprint, so a row already near the edge wraps and the side shrinks a step. */
  it('lets a crowded row re-wrap when its permanents tap', () => {
    const untapped = side({ creatures: Array.from({ length: 8 }, (_, i) => indexed(`c${i}`)) });
    const tapped = side({ creatures: Array.from({ length: 8 }, (_, i) => indexed(`c${i}`, true)) });

    expect(model.sideZoom(tapped, AREA)).toBeLessThan(model.sideZoom(untapped, AREA));
  });

  it('measures a one-land stack exactly like the bare land it replaced', () => {
    const bare = side({ lands: [indexed('forest')] });
    const stacked = side({ lands: [landStack(['forest'])] });

    expect(model.sideHeight(stacked, 0.8, AREA.width)).toBe(model.sideHeight(bare, 0.8, AREA.width));
  });

  it('reserves less room for a stack of basics than for the same lands spread out', () => {
    const ids = ['f1', 'f2', 'f3', 'f4'];
    const spread = side({ lands: ids.map(id => indexed(id)) });
    const stacked = side({ lands: [landStack(ids)] });

    expect(model.sideZoom(stacked, AREA)).toBeGreaterThanOrEqual(model.sideZoom(spread, AREA));
  });

  it('counts tucked cards against the fit so an enchanted creature reserves its aura strip', () => {
    const withAuras = new BattlefieldFitModel(() => 2);
    const creatures = Array.from({ length: 6 }, (_, i) => indexed(`c${i}`));

    expect(withAuras.sideHeight(side({ creatures }), 0.8, AREA.width))
      .toBeGreaterThan(model.sideHeight(side({ creatures }), 0.8, AREA.width));
  });

  it('falls back to natural size before the area has been measured', () => {
    const unmeasured = { width: 0, height: 0 };

    expect(model.sideZoom(side({ creatures: [indexed('bears')] }), unmeasured)).toBe(1);
    expect(model.boardZoom([side(), side()], unmeasured)).toBe(1);
  });

  it('fits the board to the taller half, so the board zoom tracks the fuller side', () => {
    const empty = side({ isEmpty: true });
    const full = side({ creatures: Array.from({ length: 30 }, (_, i) => indexed(`c${i}`)) });

    expect(model.boardZoom([empty, full], AREA)).toBe(model.boardZoom([full, full], AREA));
  });

  it('tightens the hand only once it holds more than a screenful', () => {
    expect(BattlefieldFitModel.handZoom(9)).toBeGreaterThan(BattlefieldFitModel.handZoom(10));
  });
});
