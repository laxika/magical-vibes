import { Card, Permanent } from '../../services/websocket.service';
import { canFormAttackingBand, splitBattlefield } from './battlefield.utils';

/** Minimal Permanent factory for the battlefield utility tests. */
function perm(over: {
  id: string;
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
      name: over.id,
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
