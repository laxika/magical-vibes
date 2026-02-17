import { Permanent } from '../../services/websocket.service';

export interface IndexedPermanent {
  perm: Permanent;
  originalIndex: number;
}

export interface CombatBlocker {
  index: number;
  perm: Permanent;
  isMine: boolean;
}

export interface CombatGroup {
  attackerIndex: number;
  attacker: Permanent;
  attackerIsMine: boolean;
  blockers: CombatBlocker[];
}

export interface AttachedAura {
  perm: Permanent;
  originalIndex: number;
  isMine: boolean;
}

export interface LandStack {
  lands: IndexedPermanent[];
  name: string;
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
      // Group by name + tapped state so tapped/untapped form separate stacks
      const key = ip.perm.card.name + (ip.perm.tapped ? ':tapped' : ':untapped');
      if (!basicGroups.has(key)) {
        basicGroups.set(key, []);
      }
      basicGroups.get(key)!.push(ip);
    } else {
      nonBasic.push(ip);
    }
  }

  // Create stacks for basic lands (max 4 per stack)
  for (const [, group] of basicGroups) {
    for (let i = 0; i < group.length; i += MAX_STACK) {
      const chunk = group.slice(i, i + MAX_STACK);
      if (chunk.length === 1) {
        result.push(chunk[0]);
      } else {
        result.push({ lands: chunk, name: chunk[0].perm.card.name });
      }
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
  return perm.card.type === 'CREATURE' || perm.animatedCreature;
}
