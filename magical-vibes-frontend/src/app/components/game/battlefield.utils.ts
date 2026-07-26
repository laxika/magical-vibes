import { Permanent } from '../../services/websocket.service';
import { hasCardType } from '../../utils/format-utils';

export interface IndexedPermanent {
  perm: Permanent;
  originalIndex: number;
}

export interface AttachedAura {
  perm: Permanent;
  originalIndex: number;
  isMine: boolean;
}

export interface LandStack {
  lands: IndexedPermanent[];
  name: string;
  /** Identity for the @for track: the stack's SLOT, not any land in it, so a stack keeps
   *  its DOM (and its neighbours keep theirs) as lands join and leave it. */
  key: string;
}

/**
 * Whether the selected attackers include enough creatures to form a band and at least one of them
 * currently has banding. The other selected creature may lack banding (CR 702.22c).
 */
export function canFormAttackingBand(battlefield: Permanent[], attackerIndices: Set<number>): boolean {
  const attackers = Array.from(attackerIndices)
    .map(index => battlefield[index])
    .filter((permanent): permanent is Permanent => permanent != null);

  return attackers.length >= 2 && attackers.some(permanent => hasEffectiveKeyword(permanent, 'BANDING'));
}

function hasEffectiveKeyword(permanent: Permanent, keyword: string): boolean {
  if (permanent.removedKeywords?.includes(keyword)) return false;
  return permanent.card.keywords?.includes(keyword) || permanent.grantedKeywords?.includes(keyword);
}

export function splitBattlefield(battlefield: Permanent[]): { lands: IndexedPermanent[], creatures: IndexedPermanent[] } {
  const lands: IndexedPermanent[] = [];
  const creatures: IndexedPermanent[] = [];
  battlefield.forEach((perm, idx) => {
    if (perm.attachedTo != null) return; // Auras rendered with their host
    const entry: IndexedPermanent = { perm, originalIndex: idx };
    if (isPermanentCreature(perm)) {
      creatures.push(entry);
    } else {
      lands.push(entry);
    }
  });
  return { lands, creatures };
}

export function stackBasicLands(lands: IndexedPermanent[]): (IndexedPermanent | LandStack)[] {
  const MAX_STACK = 4;
  const result: (IndexedPermanent | LandStack)[] = [];
  const basicGroups = new Map<string, IndexedPermanent[]>();
  const nonBasic: IndexedPermanent[] = [];

  for (const ip of lands) {
    if (ip.perm.card.type === 'LAND' && ip.perm.card.supertypes?.includes('BASIC')) {
      // Group by name only: a land stays in its stack when tapped and just
      // rotates in place, instead of teleporting to a separate tapped stack.
      const key = ip.perm.card.name;
      if (!basicGroups.has(key)) {
        basicGroups.set(key, []);
      }
      basicGroups.get(key)!.push(ip);
    } else {
      nonBasic.push(ip);
    }
  }

  /* Create stacks for basic lands (max 4 per stack). A group of ONE is still a stack.
     It renders identically either way — a one-card fan has no overlap to draw — but a
     bare land and a stack are different branches of the template, so emitting a bare
     land for the first Forest and a stack for the second would destroy and rebuild BOTH
     elements the moment the second one arrives. That is invisible while nothing moves
     and very visible once anything does: the newly played land appeared beside the fan
     and then jumped into it. Keeping the shape constant means playing a second Forest
     simply appends one item to a stack that is already there. */
  for (const [name, group] of basicGroups) {
    for (let i = 0; i < group.length; i += MAX_STACK) {
      result.push({
        lands: group.slice(i, i + MAX_STACK),
        name,
        key: `${name}#${i / MAX_STACK}`,
      });
    }
  }

  // Non-basic lands remain individual
  for (const ip of nonBasic) {
    result.push(ip);
  }

  return result;
}

export function getAttachedAuras(permanentId: string, myBattlefield: Permanent[], opponentBattlefield: Permanent[]): AttachedAura[] {
  const auras: AttachedAura[] = [];
  myBattlefield.forEach((perm, idx) => {
    if (perm.attachedTo === permanentId) {
      auras.push({ perm, originalIndex: idx, isMine: true });
    }
  });
  opponentBattlefield.forEach((perm, idx) => {
    if (perm.attachedTo === permanentId) {
      auras.push({ perm, originalIndex: idx, isMine: false });
    }
  });
  return auras;
}

export function isLandStack(item: IndexedPermanent | LandStack): item is LandStack {
  return 'lands' in item;
}

export function isPermanentCreature(perm: Permanent): boolean {
  return hasCardType(perm.card, 'CREATURE') || perm.animatedCreature;
}

export function isPermanentArtifact(perm: Permanent): boolean {
  return hasCardType(perm.card, 'ARTIFACT');
}

export function isPermanentLand(perm: Permanent): boolean {
  return hasCardType(perm.card, 'LAND');
}
