import { inject, Injectable, Signal, signal } from '@angular/core';
import {
  WebsocketService, Game, MessageType, Card, Permanent,
  ChooseCardFromHandNotification, ChooseColorNotification, MayAbilityNotification,
  ChoosePermanentNotification, ChooseMultiplePermanentsNotification,
  ChooseMultipleCardsFromGraveyardsNotification, ScryNotification, ReorderLibraryCardsNotification,
  ChooseCardFromLibraryNotification, RevealHandNotification,
  ChooseFromRevealedHandNotification, ChooseCardFromGraveyardNotification,
  ChooseHandTopBottomNotification, CombatDamageAssignmentNotification,
  ValidTargetsResponse, XValueChoiceNotification
} from './websocket.service';
import { TargetingChoiceService } from './targeting-choice.service';
import { LibraryChoiceService } from './library-choice.service';
import { DamageChoiceService } from './damage-choice.service';
import { isPermanentCreature } from '../components/game/battlefield.utils';

@Injectable({ providedIn: 'root' })
export class GameChoiceService {

  constructor(private websocketService: WebsocketService) {}

  readonly targeting = inject(TargetingChoiceService);
  readonly library = inject(LibraryChoiceService);
  readonly damage = inject(DamageChoiceService);

  private gameSignal!: Signal<Game | null>;

  init(
    gameSignal: Signal<Game | null>,
    myBattlefieldFn: () => Permanent[],
    opponentBattlefieldFn: () => Permanent[],
    totalManaFn: () => number
  ): void {
    this.reset();
    this.gameSignal = gameSignal;
    this.targeting.init(gameSignal, myBattlefieldFn, opponentBattlefieldFn, totalManaFn);
  }

  reset(): void {
    // Hand choice
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
    // Color choice
    this.choosingColor = false;
    this.colorChoices = [];
    this.colorChoicePrompt = '';
    // May ability
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
    this.mayAbilityCanPay = true;
    this.mayAbilityManaCost = null;
    // Permanent choice
    this.choosingPermanent = false;
    this.choosablePermanentIds.set(new Set());
    this.choosablePlayerIds.set(new Set());
    this.permanentChoicePrompt = '';
    // Multi-permanent
    this.choosingMultiplePermanents = false;
    this.multiPermanentChoiceIds.set(new Set());
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = 0;
    this.multiPermanentChoicePrompt = '';
    // Multi-graveyard
    this.choosingGraveyardCards = false;
    this.multiGraveyardCards = [];
    this.graveyardChoiceCardIds = [];
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = 0;
    this.multiGraveyardPrompt = '';
    // Reveal hand
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandPlayerName = '';
    // Choose from revealed hand
    this.choosingFromRevealedHand = false;
    this.revealedHandChoosableIndices = new Set();
    this.revealedHandChoicePrompt = '';
    // Choose from graveyard
    this.choosingFromGraveyard = false;
    this.graveyardChoiceIndices = [];
    this.graveyardChoicePrompt = '';
    this.graveyardChoiceAllGraveyards = false;
    // X value choice
    this.awaitingXValueChoice = false;
    this.xValueChoicePrompt = '';
    this.xValueChoiceMaxValue = 0;
    this.xValueChoiceInput = 0;
    // Sub-services
    this.targeting.reset();
    this.library.reset();
    this.damage.reset();
  }

  // --- Hand choice state ---
  choosingFromHand = false;
  choosableHandIndices = signal(new Set<number>());
  handChoicePrompt = '';

  // --- Color choice state ---
  choosingColor = false;
  colorChoices: string[] = [];
  colorChoicePrompt = '';

  // --- May ability state ---
  awaitingMayAbility = false;
  mayAbilityPrompt = '';
  mayAbilityCanPay = true;
  mayAbilityManaCost: string | null = null;

  // --- Permanent choice state ---
  choosingPermanent = false;
  choosablePermanentIds = signal(new Set<string>());
  choosablePlayerIds = signal(new Set<string>());
  permanentChoicePrompt = '';

  // --- Multi-permanent state ---
  choosingMultiplePermanents = false;
  multiPermanentChoiceIds = signal(new Set<string>());
  multiPermanentSelectedIds = signal(new Set<string>());
  multiPermanentMaxCount = 0;
  multiPermanentChoicePrompt = '';

  // --- Multi-graveyard choice state ---
  choosingGraveyardCards = false;
  multiGraveyardCards: Card[] = [];
  graveyardChoiceCardIds: string[] = [];
  graveyardChoiceSelectedIds = signal(new Set<string>());
  graveyardChoiceMaxCount = 0;
  multiGraveyardPrompt = '';

  // --- Reveal hand state ---
  revealingHand = false;
  revealedHandCards: Card[] = [];
  revealedHandPlayerName = '';

  // --- Choose from revealed hand state ---
  choosingFromRevealedHand = false;
  revealedHandChoosableIndices = new Set<number>();
  revealedHandChoicePrompt = '';

  // --- Choose from graveyard state ---
  choosingFromGraveyard = false;
  graveyardChoiceIndices: number[] = [];
  graveyardChoicePrompt = '';
  graveyardChoiceAllGraveyards = false;

  // --- X value choice state ---
  awaitingXValueChoice = false;
  xValueChoicePrompt = '';
  xValueChoiceMaxValue = 0;
  xValueChoiceInput = 0;

  // ========== Message handlers ==========

  handleChooseCardFromHand(msg: ChooseCardFromHandNotification): void {
    this.choosingFromHand = true;
    this.choosableHandIndices.set(new Set(msg.cardIndices));
    this.handChoicePrompt = msg.prompt;
  }

  handleChooseColor(msg: ChooseColorNotification): void {
    this.choosingColor = true;
    this.colorChoices = msg.colors;
    this.colorChoicePrompt = msg.prompt;
  }

  handleMayAbilityChoice(msg: MayAbilityNotification): void {
    this.awaitingMayAbility = true;
    this.mayAbilityPrompt = msg.prompt;
    this.mayAbilityCanPay = msg.canPay;
    this.mayAbilityManaCost = msg.manaCost;
  }

  handleChoosePermanent(msg: ChoosePermanentNotification): void {
    this.choosingPermanent = true;
    this.choosablePermanentIds.set(new Set(msg.permanentIds));
    this.choosablePlayerIds.set(new Set(msg.playerIds ?? []));
    this.permanentChoicePrompt = msg.prompt;
  }

  handleChooseMultiplePermanents(msg: ChooseMultiplePermanentsNotification): void {
    this.choosingMultiplePermanents = true;
    this.multiPermanentChoiceIds.set(new Set(msg.permanentIds));
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = msg.maxCount;
    this.multiPermanentChoicePrompt = msg.prompt;
  }

  handleChooseMultipleCardsFromGraveyards(msg: ChooseMultipleCardsFromGraveyardsNotification): void {
    this.choosingGraveyardCards = true;
    this.multiGraveyardCards = msg.cards;
    this.graveyardChoiceCardIds = msg.cardIds;
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = msg.maxCount;
    this.multiGraveyardPrompt = msg.prompt;
  }

  handleRevealHand(msg: RevealHandNotification): void {
    this.revealingHand = true;
    this.revealedHandCards = msg.cards;
    this.revealedHandPlayerName = msg.playerName;
  }

  handleChooseFromRevealedHand(msg: ChooseFromRevealedHandNotification): void {
    this.revealingHand = true;
    this.choosingFromRevealedHand = true;
    this.revealedHandCards = msg.cards;
    this.revealedHandChoosableIndices = new Set(msg.validIndices);
    this.revealedHandChoicePrompt = msg.prompt;
    this.revealedHandPlayerName = '';
  }

  handleChooseCardFromGraveyard(msg: ChooseCardFromGraveyardNotification): void {
    this.choosingFromGraveyard = true;
    this.graveyardChoiceIndices = msg.cardIndices;
    this.graveyardChoicePrompt = msg.prompt;
    this.graveyardChoiceAllGraveyards = msg.allGraveyards;
  }

  handleXValueChoice(msg: XValueChoiceNotification): void {
    this.awaitingXValueChoice = true;
    this.xValueChoicePrompt = msg.prompt;
    this.xValueChoiceMaxValue = msg.maxValue;
    this.xValueChoiceInput = msg.maxValue;
  }

  // --- Delegated handlers ---

  handleScry(msg: ScryNotification): void {
    this.library.handleScry(msg);
  }

  handleReorderLibraryCards(msg: ReorderLibraryCardsNotification): void {
    this.library.handleReorderLibraryCards(msg);
  }

  handleChooseCardFromLibrary(msg: ChooseCardFromLibraryNotification): void {
    this.library.handleChooseCardFromLibrary(msg);
  }

  handleChooseHandTopBottom(msg: ChooseHandTopBottomNotification): void {
    this.library.handleChooseHandTopBottom(msg);
  }

  handleCombatDamageAssignment(msg: CombatDamageAssignmentNotification): void {
    this.damage.handleCombatDamageAssignment(msg);
  }

  handleValidTargetsResponse(msg: ValidTargetsResponse): void {
    this.targeting.handleValidTargetsResponse(msg);
  }

  // ========== User actions ==========

  chooseCardFromHand(index: number): void {
    const g = this.gameSignal();
    if (!g || !this.choosingFromHand) return;
    if (!this.choosableHandIndices().has(index)) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: index
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  declineHandChoice(): void {
    const g = this.gameSignal();
    if (!g || !this.choosingFromHand) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: -1
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  chooseColor(color: string): void {
    if (!this.choosingColor) return;
    this.websocketService.send({
      type: MessageType.COLOR_CHOSEN,
      color: color
    });
    this.choosingColor = false;
    this.colorChoices = [];
    this.colorChoicePrompt = '';
  }

  acceptMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.MAY_ABILITY_CHOSEN,
      accepted: true
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
    this.mayAbilityCanPay = true;
    this.mayAbilityManaCost = null;
  }

  declineMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.MAY_ABILITY_CHOSEN,
      accepted: false
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
    this.mayAbilityCanPay = true;
    this.mayAbilityManaCost = null;
  }

  confirmXValueChoice(): void {
    if (!this.awaitingXValueChoice) return;
    this.websocketService.send({
      type: MessageType.X_VALUE_CHOSEN,
      chosenValue: this.xValueChoiceInput
    });
    this.awaitingXValueChoice = false;
    this.xValueChoicePrompt = '';
    this.xValueChoiceMaxValue = 0;
    this.xValueChoiceInput = 0;
  }

  cancelXValueChoice(): void {
    if (!this.awaitingXValueChoice) return;
    this.websocketService.send({
      type: MessageType.X_VALUE_CHOSEN,
      chosenValue: 0
    });
    this.awaitingXValueChoice = false;
    this.xValueChoicePrompt = '';
    this.xValueChoiceMaxValue = 0;
    this.xValueChoiceInput = 0;
  }

  choosePermanent(permanentId: string): void {
    if (!this.choosingPermanent) return;
    if (!this.choosablePermanentIds().has(permanentId) && !this.choosablePlayerIds().has(permanentId)) return;
    this.websocketService.send({
      type: MessageType.PERMANENT_CHOSEN,
      permanentId: permanentId
    });
    this.choosingPermanent = false;
    this.choosablePermanentIds.set(new Set());
    this.choosablePlayerIds.set(new Set());
    this.permanentChoicePrompt = '';
  }

  toggleMultiPermanentSelection(permanentId: string): void {
    if (!this.choosingMultiplePermanents) return;
    if (!this.multiPermanentChoiceIds().has(permanentId)) return;
    const selected = new Set(this.multiPermanentSelectedIds());
    if (selected.has(permanentId)) {
      selected.delete(permanentId);
    } else if (selected.size < this.multiPermanentMaxCount) {
      selected.add(permanentId);
    }
    this.multiPermanentSelectedIds.set(selected);
  }

  confirmMultiPermanentChoice(): void {
    if (!this.choosingMultiplePermanents) return;
    this.websocketService.send({
      type: MessageType.MULTIPLE_PERMANENTS_CHOSEN,
      permanentIds: Array.from(this.multiPermanentSelectedIds())
    });
    this.choosingMultiplePermanents = false;
    this.multiPermanentChoiceIds.set(new Set());
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = 0;
    this.multiPermanentChoicePrompt = '';
  }

  toggleGraveyardCardSelection(index: number): void {
    if (!this.choosingGraveyardCards) return;
    const cardId = this.graveyardChoiceCardIds[index];
    if (!cardId) return;
    const selected = new Set(this.graveyardChoiceSelectedIds());
    if (selected.has(cardId)) {
      selected.delete(cardId);
    } else if (selected.size < this.graveyardChoiceMaxCount) {
      selected.add(cardId);
    }
    this.graveyardChoiceSelectedIds.set(selected);
  }

  confirmGraveyardCardChoice(): void {
    if (!this.choosingGraveyardCards) return;
    this.websocketService.send({
      type: MessageType.MULTIPLE_GRAVEYARD_CARDS_CHOSEN,
      cardIds: Array.from(this.graveyardChoiceSelectedIds())
    });
    this.choosingGraveyardCards = false;
    this.multiGraveyardCards = [];
    this.graveyardChoiceCardIds = [];
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = 0;
    this.multiGraveyardPrompt = '';
  }

  getColorDisplayName(color: string): string {
    switch (color) {
      case 'WHITE': return 'White';
      case 'BLUE': return 'Blue';
      case 'BLACK': return 'Black';
      case 'RED': return 'Red';
      case 'GREEN': return 'Green';
      case 'PLAINS': return 'Plains';
      case 'ISLAND': return 'Island';
      case 'SWAMP': return 'Swamp';
      case 'MOUNTAIN': return 'Mountain';
      case 'FOREST': return 'Forest';
      default: return color;
    }
  }

  isChoosableCard(index: number): boolean {
    return this.choosableHandIndices().has(index);
  }

  // ========== Reveal hand ==========

  closeRevealHand(): void {
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandPlayerName = '';
  }

  chooseFromRevealedHand(index: number): void {
    if (!this.choosingFromRevealedHand) return;
    if (!this.revealedHandChoosableIndices.has(index)) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: index
    });
    this.choosingFromRevealedHand = false;
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandChoosableIndices = new Set();
    this.revealedHandChoicePrompt = '';
  }

  isRevealedHandCardChoosable(index: number): boolean {
    return this.choosingFromRevealedHand && this.revealedHandChoosableIndices.has(index);
  }

  // ========== Mana tapping (X value choice & may ability) ==========

  canTapForMana(perm: Permanent): boolean {
    const allowTap = this.awaitingXValueChoice || (this.awaitingMayAbility && this.mayAbilityManaCost != null);
    if (!allowTap || perm.tapped) return false;
    if (perm.summoningSick && isPermanentCreature(perm)) return false;
    // Has ON_TAP mana effects (lands, mana rocks)
    if (perm.card.hasTapAbility) return true;
    // Has mana-producing activated ability (e.g., Birds of Paradise)
    return perm.card.activatedAbilities.some(a => a.isManaAbility);
  }

  updateMayAbilityCanPay(manaPool: Record<string, number>): void {
    if (!this.awaitingMayAbility || !this.mayAbilityManaCost) {
      return;
    }
    const tokens = this.mayAbilityManaCost.match(/\{([^}]+)\}/g)?.map(t => t.slice(1, -1)) ?? [];
    const coloredNeeded: Record<string, number> = {};
    let genericNeeded = 0;
    let hasX = false;

    for (const token of tokens) {
      if (/^\d+$/.test(token)) {
        genericNeeded += parseInt(token);
      } else if (token === 'X') {
        hasX = true;
      } else {
        coloredNeeded[token] = (coloredNeeded[token] ?? 0) + 1;
      }
    }

    // Check each colored requirement
    for (const [color, needed] of Object.entries(coloredNeeded)) {
      if ((manaPool[color] ?? 0) < needed) {
        this.mayAbilityCanPay = false;
        return;
      }
    }

    // Calculate remaining mana after colored costs
    const totalAvailable = Object.values(manaPool).reduce((sum, v) => sum + v, 0);
    const coloredSpent = Object.values(coloredNeeded).reduce((sum, v) => sum + v, 0);
    const remaining = totalAvailable - coloredSpent;

    this.mayAbilityCanPay = hasX ? remaining > genericNeeded : remaining >= genericNeeded;
  }

  // ========== Graveyard choice ==========

  get graveyardChoiceCards(): { card: Card; index: number; owner: string }[] {
    const g = this.gameSignal();
    if (!g) return [];
    const validIndices = new Set(this.graveyardChoiceIndices);
    const result: { card: Card; index: number; owner: string }[] = [];

    if (this.graveyardChoiceAllGraveyards) {
      let poolIndex = 0;
      for (let playerIdx = 0; playerIdx < g.graveyards.length; playerIdx++) {
        const graveyard = g.graveyards[playerIdx];
        const ownerName = g.playerNames[playerIdx] ?? 'Unknown';
        for (const card of graveyard) {
          if (card.type === 'CREATURE' || card.type === 'ARTIFACT') {
            if (validIndices.has(poolIndex)) {
              result.push({ card, index: poolIndex, owner: ownerName });
            }
            poolIndex++;
          }
        }
      }
    } else {
      const myId = this.websocketService.currentUser?.userId ?? '';
      const myPlayerIdx = g.playerIds.indexOf(myId);
      const graveyard = g.graveyards[myPlayerIdx] ?? [];
      const ownerName = g.playerNames[myPlayerIdx] ?? 'Unknown';
      for (const idx of this.graveyardChoiceIndices) {
        if (idx >= 0 && idx < graveyard.length) {
          result.push({ card: graveyard[idx], index: idx, owner: ownerName });
        }
      }
    }

    return result;
  }

  chooseGraveyardCard(index: number): void {
    if (!this.choosingFromGraveyard) return;
    this.websocketService.send({
      type: MessageType.GRAVEYARD_CARD_CHOSEN,
      cardIndex: index
    });
    this.choosingFromGraveyard = false;
    this.graveyardChoiceIndices = [];
    this.graveyardChoicePrompt = '';
    this.graveyardChoiceAllGraveyards = false;
  }
}
