import { Injectable, Signal, signal } from '@angular/core';
import {
  WebsocketService, Game, MessageType, Card, Permanent, StackEntry, ActivatedAbilityView,
  ValidTargetsResponse
} from './websocket.service';
import { isPermanentCreature } from '../components/game/battlefield.utils';

@Injectable({ providedIn: 'root' })
export class TargetingChoiceService {

  constructor(private websocketService: WebsocketService) {}

  private gameSignal!: Signal<Game | null>;
  private myBattlefieldFn!: () => Permanent[];
  private opponentBattlefieldFn!: () => Permanent[];
  private totalManaFn!: () => number;

  init(
    gameSignal: Signal<Game | null>,
    myBattlefieldFn: () => Permanent[],
    opponentBattlefieldFn: () => Permanent[],
    totalManaFn: () => number
  ): void {
    this.gameSignal = gameSignal;
    this.myBattlefieldFn = myBattlefieldFn;
    this.opponentBattlefieldFn = opponentBattlefieldFn;
    this.totalManaFn = totalManaFn;
  }

  private get hasPriority(): boolean {
    const g = this.gameSignal();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
  }

  // --- Ability picker state ---
  choosingAbility = false;
  abilityChoicePermanentIndex = -1;
  abilityChoices: { ability: ActivatedAbilityView; index: number; usable: boolean }[] = [];

  // --- X cost prompt state ---
  choosingXValue = false;
  xValueCardIndex = -1;
  xValueCardName = '';
  xValueInput = 0;
  xValueMaximum = 0;

  // --- Targeting state (for instants and activated abilities) ---
  selectingTarget = false;
  targetingCardIndex = -1;
  targetingCardName = '';
  targetingForAbility = false;
  targetingAbilityIndex = -1;
  pendingAbilityXValue: number | null = null;
  validTargetPermanentIds = signal(new Set<string>());
  validTargetPlayerIds = signal(new Set<string>());
  targetingPrompt = '';
  pendingTargetRequest = false;

  // --- Spell targeting state (for counterspells) ---
  targetingSpell = false;
  targetingSpellCardIndex = -1;
  targetingSpellCardName = '';

  // --- Multi-target state (for spells like "one or two target creatures") ---
  multiTargeting = false;
  multiTargetCardIndex = -1;
  multiTargetCardName = '';
  multiTargetMinCount = 0;
  multiTargetMaxCount = 0;
  multiTargetSelectedIds = signal<string[]>([]);

  // --- Convoke state ---
  convoking = false;
  convokeCardIndex = -1;
  convokeCardName = '';
  convokeSelectedCreatureIds = signal<string[]>([]);
  private pendingMultiTargetIds: string[] = [];
  private pendingConvokeCard: Card | null = null;

  // ========== Message handlers ==========

  handleValidTargetsResponse(msg: ValidTargetsResponse): void {
    this.pendingTargetRequest = false;
    this.validTargetPermanentIds.set(new Set(msg.validPermanentIds));
    this.validTargetPlayerIds.set(new Set(msg.validPlayerIds));
    this.targetingPrompt = msg.prompt;

    if (msg.maxTargets > 1) {
      // Multi-target mode
      this.multiTargeting = true;
      this.multiTargetMinCount = msg.minTargets;
      this.multiTargetMaxCount = msg.maxTargets;
      this.multiTargetSelectedIds.set([]);
    } else {
      // Single target mode
      this.selectingTarget = true;
    }
  }

  // ========== Play card / targeting / abilities ==========

  private sendValidTargetsRequest(cardIndex: number | null, permanentIndex: number | null, abilityIndex: number | null, alreadySelectedIds: string[] = []): void {
    this.pendingTargetRequest = true;
    this.websocketService.send({
      type: MessageType.VALID_TARGETS_REQUEST,
      cardIndex,
      permanentIndex,
      abilityIndex,
      alreadySelectedIds
    });
  }

  playCard(index: number, isCardPlayable: (i: number) => boolean): void {
    const g = this.gameSignal();
    if (g && isCardPlayable(index)) {
      const card = g.hand[index];
      const hasXCost = card.manaCost?.includes('{X}') ?? false;

      if (hasXCost) {
        const baseCost = (card.manaCost ?? '').replace('{X}', '');
        let base = 0;
        const matches = baseCost.match(/\{([^}]+)\}/g) || [];
        for (const m of matches) {
          const inner = m.slice(1, -1);
          const num = parseInt(inner);
          base += isNaN(num) ? 1 : num;
        }
        this.choosingXValue = true;
        this.xValueCardIndex = index;
        this.xValueCardName = card.name;
        this.xValueInput = 0;
        this.xValueMaximum = this.totalManaFn() - base;
        return;
      }
      if (card.needsSpellTarget) {
        this.targetingSpell = true;
        this.targetingSpellCardIndex = index;
        this.targetingSpellCardName = card.name;
        return;
      }
      if (card.needsTarget) {
        // Ask backend for valid targets
        this.targetingCardIndex = index;
        this.targetingCardName = card.name;
        this.targetingForAbility = false;
        this.targetingAbilityIndex = -1;
        this.pendingAbilityXValue = null;
        this.pendingConvokeCard = card.hasConvoke ? card : null;
        this.sendValidTargetsRequest(index, null, null);
        return;
      }
      // No targets needed — check for convoke
      if (card.hasConvoke) {
        this.pendingConvokeCard = card;
        this.pendingMultiTargetIds = [];
        this.enterConvokeMode(index, card);
        return;
      }
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetPermanentId: null });
    }
  }

  confirmXValue(): void {
    const g = this.gameSignal();
    if (!g) return;

    if (this.targetingForAbility) {
      const perm = this.myBattlefieldFn()[this.xValueCardIndex];
      const ability = perm?.card.activatedAbilities[this.targetingAbilityIndex];
      if (ability?.needsTarget) {
        // Store X value and request valid targets from backend
        this.pendingAbilityXValue = this.xValueInput;
        this.choosingXValue = false;
        this.targetingCardIndex = this.xValueCardIndex;
        this.targetingCardName = this.xValueCardName;
        this.sendValidTargetsRequest(null, this.xValueCardIndex, this.targetingAbilityIndex);
        return;
      }
      // X value only, no target
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.xValueCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        xValue: this.xValueInput
      });
    } else {
      const card = g.hand[this.xValueCardIndex];
      if (card?.needsTarget) {
        // Store X value and request valid targets from backend
        const savedXValue = this.xValueInput;
        const savedCardIndex = this.xValueCardIndex;
        const savedCardName = this.xValueCardName;
        this.choosingXValue = false;
        this.targetingCardIndex = savedCardIndex;
        this.targetingCardName = savedCardName;
        this.targetingForAbility = false;
        this.targetingAbilityIndex = -1;
        this.pendingAbilityXValue = savedXValue;
        this.pendingConvokeCard = null;
        this.xValueCardIndex = -1;
        this.xValueCardName = '';
        this.sendValidTargetsRequest(savedCardIndex, null, null);
        return;
      }
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.xValueCardIndex,
        xValue: this.xValueInput,
        targetPermanentId: null
      });
    }
    this.choosingXValue = false;
    this.xValueCardIndex = -1;
    this.xValueCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  cancelXValue(): void {
    this.choosingXValue = false;
    this.xValueCardIndex = -1;
    this.xValueCardName = '';
    this.xValueInput = 0;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  selectTarget(permanentId: string): void {
    if (!this.selectingTarget) return;
    if (!this.validTargetPermanentIds().has(permanentId)) return;
    if (this.targetingForAbility) {
      const msg: any = {
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: permanentId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.websocketService.send(msg);
    } else if (this.pendingConvokeCard?.hasConvoke) {
      // Single-target spell with convoke — save target and enter convoke mode
      const cardIndex = this.targetingCardIndex;
      const card = this.pendingConvokeCard;
      this.pendingMultiTargetIds = [permanentId];
      this.resetTargetingState();
      this.enterConvokeMode(cardIndex, card);
      return;
    } else {
      const msg: any = {
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingCardIndex,
        targetPermanentId: permanentId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.websocketService.send(msg);
    }
    this.resetTargetingState();
  }

  selectPlayerTarget(playerIndex: number): void {
    if (!this.selectingTarget) return;
    const g = this.gameSignal();
    if (!g) return;
    const playerId = g.playerIds[playerIndex];
    if (!this.validTargetPlayerIds().has(playerId)) return;
    if (this.targetingForAbility) {
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: playerId
      });
    } else {
      const msg: any = {
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingCardIndex,
        targetPermanentId: playerId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.websocketService.send(msg);
    }
    this.resetTargetingState();
  }

  private resetTargetingState(): void {
    this.selectingTarget = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.validTargetPermanentIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = '';
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
  }

  cancelTargeting(): void {
    this.resetTargetingState();
  }

  selectSpellTarget(entry: StackEntry): void {
    if (!this.targetingSpell || !entry.isSpell) return;
    if (this.targetingForAbility) {
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingSpellCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: entry.cardId
      });
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingSpellCardIndex,
        targetPermanentId: entry.cardId
      });
    }
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  cancelSpellTargeting(): void {
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  isValidTarget(perm: Permanent): boolean {
    return this.validTargetPermanentIds().has(perm.id);
  }

  // ========== Multi-target selection ==========

  addMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    const current = this.multiTargetSelectedIds();
    if (current.includes(permanentId)) return;
    if (current.length >= this.multiTargetMaxCount) return;
    if (!this.validTargetPermanentIds().has(permanentId)) return;
    this.multiTargetSelectedIds.set([...current, permanentId]);
  }

  addMultiTargetPlayer(playerIndex: number): void {
    if (!this.multiTargeting) return;
    const g = this.gameSignal();
    if (!g) return;
    const playerId = g.playerIds[playerIndex];
    if (!this.validTargetPlayerIds().has(playerId)) return;
    const current = this.multiTargetSelectedIds();
    if (current.includes(playerId)) return;
    if (current.length >= this.multiTargetMaxCount) return;
    this.multiTargetSelectedIds.set([...current, playerId]);
  }

  removeMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    const newSelected = this.multiTargetSelectedIds().filter(id => id !== permanentId);
    this.multiTargetSelectedIds.set(newSelected);
    // Request refreshed valid targets from backend with updated already-selected list
    this.sendValidTargetsRequest(this.multiTargetCardIndex, null, null, newSelected);
  }

  confirmMultiTargets(): void {
    if (!this.multiTargeting) return;
    const selected = this.multiTargetSelectedIds();
    if (selected.length < this.multiTargetMinCount) return;

    const card = this.pendingConvokeCard;
    this.pendingMultiTargetIds = [...selected];

    this.multiTargeting = false;
    this.multiTargetSelectedIds.set([]);
    this.validTargetPermanentIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());

    // If card has convoke, enter convoke mode
    if (card?.hasConvoke) {
      this.enterConvokeMode(this.multiTargetCardIndex, card);
      return;
    }

    // Send directly
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.multiTargetCardIndex,
      targetPermanentIds: this.pendingMultiTargetIds
    });
    this.resetMultiTargetState();
  }

  cancelMultiTargeting(): void {
    this.multiTargeting = false;
    this.multiTargetCardIndex = -1;
    this.multiTargetCardName = '';
    this.multiTargetSelectedIds.set([]);
    this.validTargetPermanentIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());
    this.pendingConvokeCard = null;
  }

  isMultiTargetSelected(permanentId: string): boolean {
    return this.multiTargetSelectedIds().includes(permanentId);
  }

  private resetMultiTargetState(): void {
    this.multiTargetCardIndex = -1;
    this.multiTargetCardName = '';
    this.multiTargetMinCount = 0;
    this.multiTargetMaxCount = 0;
    this.pendingMultiTargetIds = [];
    this.pendingConvokeCard = null;
  }

  // ========== Convoke selection ==========

  private enterConvokeMode(cardIndex: number, card: Card): void {
    this.convoking = true;
    this.convokeCardIndex = cardIndex;
    this.convokeCardName = card.name;
    this.convokeSelectedCreatureIds.set([]);
  }

  toggleConvokeCreature(permanentId: string): void {
    if (!this.convoking) return;
    const current = this.convokeSelectedCreatureIds();
    if (current.includes(permanentId)) {
      this.convokeSelectedCreatureIds.set(current.filter(id => id !== permanentId));
    } else {
      this.convokeSelectedCreatureIds.set([...current, permanentId]);
    }
  }

  isConvokeSelected(permanentId: string): boolean {
    return this.convokeSelectedCreatureIds().includes(permanentId);
  }

  confirmConvoke(): void {
    if (!this.convoking) return;
    const msg: any = {
      type: MessageType.PLAY_CARD,
      cardIndex: this.convokeCardIndex,
      convokeCreatureIds: this.convokeSelectedCreatureIds()
    };
    this.addPendingTargetsToMsg(msg);
    this.websocketService.send(msg);
    this.cancelConvoke();
    this.resetMultiTargetState();
  }

  skipConvoke(): void {
    if (!this.convoking) return;
    const msg: any = {
      type: MessageType.PLAY_CARD,
      cardIndex: this.convokeCardIndex
    };
    this.addPendingTargetsToMsg(msg);
    this.websocketService.send(msg);
    this.cancelConvoke();
    this.resetMultiTargetState();
  }

  private addPendingTargetsToMsg(msg: any): void {
    if (this.pendingMultiTargetIds.length > 0) {
      if (this.pendingConvokeCard && this.multiTargetMaxCount > 1) {
        msg.targetPermanentIds = this.pendingMultiTargetIds;
      } else {
        // Single-target card that went through convoke flow
        msg.targetPermanentId = this.pendingMultiTargetIds[0];
      }
    }
  }

  cancelConvoke(): void {
    this.convoking = false;
    this.convokeCardIndex = -1;
    this.convokeCardName = '';
    this.convokeSelectedCreatureIds.set([]);
  }

  // ========== Tap / ability activation ==========

  tapPermanent(index: number): void {
    const g = this.gameSignal();
    if (g && this.canTapPermanent(index)) {
      const perm = this.myBattlefieldFn()[index];
      if (!perm) return;

      const abilities = perm.card.activatedAbilities;
      if (abilities.length === 0) {
        // No activated abilities — just tap for mana (ON_TAP)
        this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
        return;
      }

      // Filter to usable abilities
      const usable = abilities.filter(a => this.canUseAbility(perm, a));
      if (usable.length === 0) {
        // Has abilities but none usable — fall back to tap for mana if ON_TAP
        if (perm.card.hasTapAbility && !perm.tapped) {
          this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
        }
        return;
      }

      if (usable.length === 1) {
        // Single usable ability — activate directly
        const abilityIndex = abilities.indexOf(usable[0]);
        this.activateAbilityAtIndex(index, abilityIndex, perm);
      } else {
        // Multiple usable abilities — show picker
        this.choosingAbility = true;
        this.abilityChoicePermanentIndex = index;
        this.abilityChoices = abilities.map((a, i) => ({ ability: a, index: i, usable: this.canUseAbility(perm, a) }));
      }
    }
  }

  canUseAbility(perm: Permanent, ability: ActivatedAbilityView): boolean {
    if (ability.loyaltyCost != null) {
      const g = this.gameSignal();
      if (!g) return false;
      const myId = this.websocketService.currentUser?.userId;
      // Sorcery-speed: must be active player
      if (g.activePlayerId !== myId) return false;
      // Main phase only
      if (g.currentStep !== 'PRECOMBAT_MAIN' && g.currentStep !== 'POSTCOMBAT_MAIN') return false;
      // Stack must be empty
      if (g.stack.length > 0) return false;
      // Negative loyalty cost: check sufficient loyalty
      if (ability.loyaltyCost < 0 && perm.loyaltyCounters < Math.abs(ability.loyaltyCost)) return false;
      return true;
    }
    if (ability.requiresTap) {
      if (perm.tapped) return false;
      if (perm.summoningSick && isPermanentCreature(perm)) return false;
    }
    if (ability.manaCost && !this.canPayManaCost(ability.manaCost)) return false;
    return true;
  }

  private canPayManaCost(manaCost: string): boolean {
    const g = this.gameSignal();
    if (!g) return false;
    const pool = g.manaPool;
    const symbols = manaCost.match(/\{([^}]+)\}/g) || [];
    const coloredSymbols = ['W', 'U', 'B', 'R', 'G', 'C'];
    const coloredNeeded: Record<string, number> = {};
    let genericNeeded = 0;
    for (const sym of symbols) {
      const inner = sym.slice(1, -1);
      if (inner === 'X' || inner === 'T') continue;
      if (coloredSymbols.includes(inner)) {
        coloredNeeded[inner] = (coloredNeeded[inner] ?? 0) + 1;
      } else {
        const num = parseInt(inner);
        if (!isNaN(num)) genericNeeded += num;
      }
    }
    // Check each colored requirement
    let totalUsed = 0;
    for (const [color, needed] of Object.entries(coloredNeeded)) {
      if ((pool[color] ?? 0) < needed) return false;
      totalUsed += needed;
    }
    // Check generic requirement against remaining mana
    const totalAvailable = Object.values(pool).reduce((sum, v) => sum + v, 0);
    if (totalAvailable - totalUsed < genericNeeded) return false;
    return true;
  }

  activateAbilityAtIndex(permanentIndex: number, abilityIndex: number, perm: Permanent): void {
    const ability = perm.card.activatedAbilities[abilityIndex];

    // Check for X cost
    const hasXCost = ability.manaCost?.includes('{X}') ?? false;
    if (hasXCost) {
      const baseCost = (ability.manaCost ?? '').replace('{X}', '');
      let base = 0;
      const matches = baseCost.match(/\{([^}]+)\}/g) || [];
      for (const m of matches) {
        const inner = m.slice(1, -1);
        const num = parseInt(inner);
        base += isNaN(num) ? 1 : num;
      }
      this.choosingXValue = true;
      this.xValueCardIndex = permanentIndex;
      this.xValueCardName = perm.card.name;
      this.xValueInput = 0;
      this.xValueMaximum = this.totalManaFn() - base;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Check for spell targeting (counter spells from abilities)
    if (ability.needsSpellTarget) {
      this.targetingSpell = true;
      this.targetingSpellCardIndex = permanentIndex;
      this.targetingSpellCardName = perm.card.name;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Check for targeting — request valid targets from backend
    if (ability.needsTarget) {
      this.targetingCardIndex = permanentIndex;
      this.targetingCardName = perm.card.name;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      this.pendingAbilityXValue = null;
      this.sendValidTargetsRequest(null, permanentIndex, abilityIndex);
      return;
    }

    // No target or X needed — send immediately
    this.websocketService.send({
      type: MessageType.ACTIVATE_ABILITY,
      permanentIndex,
      abilityIndex
    });
  }

  chooseAbility(choice: { ability: ActivatedAbilityView; index: number; usable: boolean }): void {
    if (!choice.usable) return;
    const perm = this.myBattlefieldFn()[this.abilityChoicePermanentIndex];
    if (!perm) return;
    this.activateAbilityAtIndex(this.abilityChoicePermanentIndex, choice.index, perm);
    this.choosingAbility = false;
    this.abilityChoicePermanentIndex = -1;
    this.abilityChoices = [];
  }

  cancelAbilityChoice(): void {
    this.choosingAbility = false;
    this.abilityChoicePermanentIndex = -1;
    this.abilityChoices = [];
  }

  canTapPermanent(index: number): boolean {
    const perm = this.myBattlefieldFn()[index];
    if (perm == null || !this.hasPriority) return false;
    const abilities = perm.card.activatedAbilities;
    // Has a non-tap activated ability (mana-cost only) — always usable
    if (abilities.some(a => !a.requiresTap)) return true;
    if (perm.tapped) return false;
    if (!perm.card.hasTapAbility) return false;
    if (perm.summoningSick && isPermanentCreature(perm)) return false;
    return true;
  }
}
