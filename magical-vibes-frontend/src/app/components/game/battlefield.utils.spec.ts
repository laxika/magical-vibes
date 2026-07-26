import { Card, Permanent } from '../../services/websocket.service';
import { IndexedPermanent, LandStack, canFormAttackingBand, isLandStack, splitBattlefield, stackBasicLands } from './battlefield.utils';

/** Minimal Permanent factory for the battlefield utility tests. */
function perm(over: {
  id: string;
  name?: string;
  type?: string;
  attachedTo?: string | null;
  tapped?: boolean;
  supertypes?: string[];
  keywords?: string[];
  grantedKeywords?: string[];
  removedKeywords?: string[];
}): Permanent {
  return {
    id: over.id,
    tapped: over.tapped ?? false,
    attachedTo: over.attachedTo ?? null,
    animatedCreature: false,
    grantedKeywords: over.grantedKeywords ?? [],
    removedKeywords: over.removedKeywords ?? [],
    card: {
      id: over.id,
      name: over.name ?? over.id,
      type: over.type ?? 'CREATURE',
      additionalTypes: [],
      supertypes: over.supertypes ?? [],
      subtypes: [],
      keywords: over.keywords ?? [],
    } as unknown as Card,
  } as unknown as Permanent;
}

describe('splitBattlefield', () => {
  it('separates creatures from lands and skips attached auras', () => {
    const battlefield = [
      perm({ id: 'swamp', type: 'LAND', supertypes: ['BASIC'] }),
      perm({ id: 'bears', type: 'CREATURE' }),
      perm({ id: 'bracers', type: 'ENCHANTMENT', attachedTo: 'bears' }),
    ];

    const { lands, creatures } = splitBattlefield(battlefield);

    expect(lands.map((ip) => ip.perm.id)).toEqual(['swamp']);
    // The attached aura is rendered with its host, not as a standalone card.
    expect(creatures.map((ip) => ip.perm.id)).toEqual(['bears']);
  });

  // Regression guard for the "wrong creature appears tapped" bug: the battlefield
  // @for loops must track by perm.id, not originalIndex, because originalIndex is
  // purely positional and shifts when an earlier permanent leaves the battlefield.
  it('keeps perm.id stable as an identity key even though originalIndex is positional', () => {
    const before = [
      perm({ id: 'mogg', type: 'CREATURE' }),
      perm({ id: 'bracers', type: 'ENCHANTMENT', attachedTo: 'mogg' }),
      perm({ id: 'radjan', type: 'CREATURE', tapped: true }),
      perm({ id: 'shriek', type: 'CREATURE' }),
    ];

    const shriekBefore = splitBattlefield(before).creatures.find((ip) => ip.perm.id === 'shriek')!;
    expect(shriekBefore.originalIndex).toBe(3);

    // Mogg Fanatic is destroyed and its orphaned aura (Treetop Bracers) leaves too,
    // so Shriekgeist shifts down two slots in the battlefield array.
    const after = [
      perm({ id: 'radjan', type: 'CREATURE', tapped: true }),
      perm({ id: 'shriek', type: 'CREATURE' }),
    ];

    const shriekAfter = splitBattlefield(after).creatures.find((ip) => ip.perm.id === 'shriek')!;

    // originalIndex is NOT stable — this is exactly why it is unsafe as a track key.
    expect(shriekAfter.originalIndex).toBe(1);
    expect(shriekAfter.originalIndex).not.toBe(shriekBefore.originalIndex);
    // perm.id IS stable, so tracking by it keeps each card bound to its own state.
    expect(shriekAfter.perm.id).toBe(shriekBefore.perm.id);
  });
});

describe('stackBasicLands', () => {
  const basic = (id: string, name: string): IndexedPermanent => ({
    perm: perm({ id, name, type: 'LAND', supertypes: ['BASIC'] }),
    originalIndex: 0,
  });
  const keys = (items: (IndexedPermanent | LandStack)[]): string[] =>
    items.map(item => (isLandStack(item) ? `stack:${item.key}` : `land:${item.perm.id}`));

  /* The render path branches on isLandStack, so a lone basic that is NOT a stack would be
     rebuilt into one the moment a second copy is played — destroying and recreating both
     cards' elements. Keeping the shape constant is what lets the lands row animate. */
  it('emits a single basic land as a one-card stack rather than a bare land', () => {
    const result = stackBasicLands([basic('forest-1', 'Forest')]);

    expect(result).toHaveLength(1);
    expect(isLandStack(result[0])).toBe(true);
    expect((result[0] as LandStack).lands.map(ip => ip.perm.id)).toEqual(['forest-1']);
  });

  it('keeps a stack on the same key when a land joins it, so only the new card is created', () => {
    const before = stackBasicLands([basic('forest-1', 'Forest')]);
    const after = stackBasicLands([basic('forest-1', 'Forest'), basic('forest-2', 'Forest')]);

    expect(keys(after)).toEqual(keys(before));
    expect((after[0] as LandStack).lands.map(ip => ip.perm.id)).toEqual(['forest-1', 'forest-2']);
  });

  /* Keying a stack by its first land meant losing that land re-keyed the whole stack and
     rebuilt every card still in it. */
  it('keeps a stack on the same key when its FIRST land leaves', () => {
    const before = stackBasicLands([basic('forest-1', 'Forest'), basic('forest-2', 'Forest')]);
    const after = stackBasicLands([basic('forest-2', 'Forest')]);

    expect(keys(after)).toEqual(keys(before));
  });

  it('chunks at four per fan and gives each fan its own key', () => {
    const forests = ['a', 'b', 'c', 'd', 'e'].map(id => basic(`forest-${id}`, 'Forest'));

    const result = stackBasicLands(forests);

    expect(result.map(item => (item as LandStack).lands.length)).toEqual([4, 1]);
    expect(keys(result)).toEqual(['stack:Forest#0', 'stack:Forest#1']);
  });

  it('groups by name and leaves non-basic lands standalone', () => {
    const result = stackBasicLands([
      basic('forest-1', 'Forest'),
      basic('island-1', 'Island'),
      { perm: perm({ id: 'wasteland', name: 'Wasteland', type: 'LAND' }), originalIndex: 0 },
    ]);

    expect(keys(result)).toEqual(['stack:Forest#0', 'stack:Island#0', 'land:wasteland']);
    expect(isLandStack(result[2])).toBe(false);
  });
});

describe('canFormAttackingBand', () => {
  it('does not offer band controls when none of the selected attackers has banding', () => {
    const battlefield = [perm({ id: 'bear' }), perm({ id: 'wolf' })];

    expect(canFormAttackingBand(battlefield, new Set([0, 1]))).toBe(false);
  });

  it('allows one selected non-banding attacker to join an attacker with printed banding', () => {
    const battlefield = [
      perm({ id: 'wolves', keywords: ['BANDING'] }),
      perm({ id: 'bear' }),
    ];

    expect(canFormAttackingBand(battlefield, new Set([0, 1]))).toBe(true);
  });

  it('recognizes dynamically granted banding', () => {
    const battlefield = [
      perm({ id: 'bear', grantedKeywords: ['BANDING'] }),
      perm({ id: 'wolf' }),
    ];

    expect(canFormAttackingBand(battlefield, new Set([0, 1]))).toBe(true);
  });

  it('does not use banding that has been removed', () => {
    const battlefield = [
      perm({ id: 'wolves', keywords: ['BANDING'], removedKeywords: ['BANDING'] }),
      perm({ id: 'bear' }),
    ];

    expect(canFormAttackingBand(battlefield, new Set([0, 1]))).toBe(false);
  });
});
