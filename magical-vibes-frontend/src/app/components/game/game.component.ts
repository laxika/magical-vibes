import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameUpdate, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, Permanent, ActivatedAbilityView, HandDrawnNotification, MulliganResolvedNotification, GameStartedNotification, SelectCardsToBottomNotification, DeckSizesUpdatedNotification, HandSizesUpdatedNotification, PlayableCardsNotification, BattlefieldUpdatedNotification, ManaUpdatedNotification, AutoStopsUpdatedNotification, AvailableAttackersNotification, AvailableBlockersNotification, LifeUpdatedNotification, GameOverNotification, ChooseCardFromHandNotification, ChooseColorNotification, MayAbilityNotification, ChoosePermanentNotification, ChooseMultiplePermanentsNotification, StackEntry, StackUpdatedNotification, GraveyardUpdatedNotification, ReorderLibraryCardsNotification, RevealHandNotification } from '../../services/websocket.service';
import { CardDisplayComponent } from './card-display/card-display.component';
import { Subscription } from 'rxjs';

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

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, FormsModule, CardDisplayComponent],
  templateUrl: './game.component.html',
  styleUrl: './game.component.css'
})
export class GameComponent implements OnInit, OnDestroy {
  game = signal<Game | null>(null);
  hoveredCard = signal<Card | null>(null);
  hoveredPermanent = signal<Permanent | null>(null);
  stackTargetId = signal<string | null>(null);
  activeTab = signal<'log' | 'stack'>('log');
  private subscriptions: Subscription[] = [];

  constructor(
    private router: Router,
    private websocketService: WebsocketService
  ) {}

  ngOnInit() {
    if (!this.websocketService.isConnected() || !this.websocketService.currentGame) {
      this.router.navigate(['/']);
      return;
    }

    this.game.set(this.websocketService.currentGame);

    const initialStops = this.websocketService.currentGame?.autoStopSteps;
    if (initialStops) {
      this.autoStopSteps.set(new Set(initialStops));
    }

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        const notification = message as GameNotification;

        console.log(notification);

        if (notification.type === MessageType.OPPONENT_JOINED && notification.game) {
          this.game.set(notification.game);
          this.websocketService.currentGame = notification.game;
        }

        if (message.type === MessageType.GAME_LOG_ENTRY) {
          this.appendLogEntry((message as GameNotification).message!);
        }

        if (message.type === MessageType.HAND_DRAWN) {
          const handDrawn = message as HandDrawnNotification;
          this.updateHand(handDrawn.hand, handDrawn.mulliganCount);
        }

        if (message.type === MessageType.MULLIGAN_RESOLVED) {
          const resolved = message as MulliganResolvedNotification;
          this.handleMulliganResolved(resolved);
        }

        if (message.type === MessageType.GAME_STARTED) {
          const started = message as GameStartedNotification;
          this.handleGameStarted(started);
        }

        if (message.type === MessageType.SELECT_CARDS_TO_BOTTOM) {
          const selectMsg = message as SelectCardsToBottomNotification;
          this.handleSelectCardsToBottom(selectMsg);
        }

        if (message.type === MessageType.DECK_SIZES_UPDATED) {
          const deckMsg = message as DeckSizesUpdatedNotification;
          this.updateDeckSizes(deckMsg.deckSizes);
        }

        if (message.type === MessageType.HAND_SIZES_UPDATED) {
          const handMsg = message as HandSizesUpdatedNotification;
          this.updateHandSizes(handMsg.handSizes);
        }

        if (message.type === MessageType.PLAYABLE_CARDS_UPDATED) {
          const playableMsg = message as PlayableCardsNotification;
          this.playableCardIndices.set(new Set(playableMsg.playableCardIndices));
        }

        if (message.type === MessageType.BATTLEFIELD_UPDATED) {
          const bfMsg = message as BattlefieldUpdatedNotification;
          this.updateBattlefields(bfMsg.battlefields);
        }

        if (message.type === MessageType.MANA_UPDATED) {
          const manaMsg = message as ManaUpdatedNotification;
          this.updateManaPool(manaMsg.manaPool);
        }

        if (message.type === MessageType.AUTO_STOPS_UPDATED) {
          const stopsMsg = message as AutoStopsUpdatedNotification;
          this.autoStopSteps.set(new Set(stopsMsg.autoStopSteps));
        }

        if (message.type === MessageType.AVAILABLE_ATTACKERS) {
          const atkMsg = message as AvailableAttackersNotification;
          this.handleAvailableAttackers(atkMsg);
        }

        if (message.type === MessageType.AVAILABLE_BLOCKERS) {
          const blkMsg = message as AvailableBlockersNotification;
          this.handleAvailableBlockers(blkMsg);
        }

        if (message.type === MessageType.LIFE_UPDATED) {
          const lifeMsg = message as LifeUpdatedNotification;
          this.updateLifeTotals(lifeMsg.lifeTotals);
        }

        if (message.type === MessageType.GAME_OVER) {
          const goMsg = message as GameOverNotification;
          this.handleGameOver(goMsg);
        }

        if (message.type === MessageType.CHOOSE_CARD_FROM_HAND) {
          const chooseMsg = message as ChooseCardFromHandNotification;
          this.handleChooseCardFromHand(chooseMsg);
        }

        if (message.type === MessageType.CHOOSE_COLOR) {
          const colorMsg = message as ChooseColorNotification;
          this.handleChooseColor(colorMsg);
        }

        if (message.type === MessageType.MAY_ABILITY_CHOICE) {
          const mayMsg = message as MayAbilityNotification;
          this.handleMayAbilityChoice(mayMsg);
        }

        if (message.type === MessageType.STACK_UPDATED) {
          const stackMsg = message as StackUpdatedNotification;
          this.updateStack(stackMsg.stack);
        }

        if (message.type === MessageType.GRAVEYARD_UPDATED) {
          const gyMsg = message as GraveyardUpdatedNotification;
          this.updateGraveyards(gyMsg.graveyards);
        }

        if (message.type === MessageType.CHOOSE_PERMANENT) {
          const permMsg = message as ChoosePermanentNotification;
          this.handleChoosePermanent(permMsg);
        }

        if (message.type === MessageType.CHOOSE_MULTIPLE_PERMANENTS) {
          const multiPermMsg = message as ChooseMultiplePermanentsNotification;
          this.handleChooseMultiplePermanents(multiPermMsg);
        }

        if (message.type === MessageType.REORDER_LIBRARY_CARDS) {
          const reorderMsg = message as ReorderLibraryCardsNotification;
          this.handleReorderLibraryCards(reorderMsg);
        }

        if (message.type === MessageType.REVEAL_HAND) {
          const revealMsg = message as RevealHandNotification;
          this.revealingHand = true;
          this.revealedHandCards = revealMsg.cards;
          this.revealedHandPlayerName = revealMsg.playerName;
        }

        const update = message as GameUpdate;
        if (update.type === MessageType.PRIORITY_UPDATED ||
            update.type === MessageType.STEP_ADVANCED ||
            update.type === MessageType.TURN_CHANGED) {
          this.applyGameUpdate(update);
        }
      })
    );

    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        this.router.navigate(['/']);
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  get player1Name(): string {
    const g = this.game();
    return g && g.playerNames.length > 0 ? g.playerNames[0] : '';
  }

  get player2Name(): string {
    const g = this.game();
    return g && g.playerNames.length > 1 ? g.playerNames[1] : '';
  }

  get gameLog(): string[] {
    return this.game()?.gameLog ?? [];
  }

  get isWaitingForOpponent(): boolean {
    const g = this.game();
    return g !== null && g.playerNames.length < 2;
  }

  get isMyTurn(): boolean {
    const g = this.game();
    return g !== null && g.activePlayerId === this.websocketService.currentUser?.userId;
  }

  isActivePlayer(playerIndex: number): boolean {
    const g = this.game();
    return g !== null && g.playerIds?.[playerIndex] === g.activePlayerId;
  }

  holdsPriority(playerIndex: number): boolean {
    const g = this.game();
    return g !== null && g.playerIds?.[playerIndex] === g.priorityPlayerId;
  }

  get hasPriority(): boolean {
    const g = this.game();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
  }

  private applyGameUpdate(update: GameUpdate): void {
    const g = this.game();
    if (!g) return;

    // Clear playable highlights immediately on any state change;
    // the server's PLAYABLE_CARDS_UPDATED message will re-set them if appropriate
    this.playableCardIndices.set(new Set<number>());

    const updated = { ...g };
    if (update.priorityPlayerId !== undefined) {
      updated.priorityPlayerId = update.priorityPlayerId ?? null;
    }
    if (update.currentStep !== undefined) {
      updated.currentStep = update.currentStep;
    }
    if (update.activePlayerId !== undefined) {
      updated.activePlayerId = update.activePlayerId;
    }
    if (update.turnNumber !== undefined) {
      updated.turnNumber = update.turnNumber;
    }
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private appendLogEntry(entry: string): void {
    const g = this.game();
    if (!g) return;

    const updated = { ...g, gameLog: [...g.gameLog, entry] };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  get isMulliganPhase(): boolean {
    const g = this.game();
    return g !== null && g.status === GameStatus.MULLIGAN;
  }

  get canMulligan(): boolean {
    const g = this.game();
    return g !== null && g.mulliganCount < 7;
  }

  get hand(): Card[] {
    return this.game()?.hand ?? [];
  }

  private updateHand(hand: Card[], mulliganCount: number): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, hand, mulliganCount };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleMulliganResolved(resolved: MulliganResolvedNotification): void {
    const g = this.game();
    if (!g) return;

    const myName = this.websocketService.currentUser?.username;
    if (resolved.playerName === myName) {
      if (resolved.kept) {
        this.selfKept = true;
      }
    } else {
      if (resolved.kept) {
        this.opponentKept = true;
      } else {
        this.opponentKept = false;
      }
    }
  }

  private handleGameStarted(started: GameStartedNotification): void {
    const g = this.game();
    if (!g) return;
    const updated = {
      ...g,
      status: GameStatus.RUNNING,
      activePlayerId: started.activePlayerId,
      turnNumber: started.turnNumber,
      currentStep: started.currentStep,
      priorityPlayerId: started.priorityPlayerId
    };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
    this.opponentKept = false;
    this.selfKept = false;
    this.selectingBottomCards = false;
    this.bottomCardCount = 0;
    this.selectedCardIndices.clear();
  }

  private updateDeckSizes(deckSizes: number[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, deckSizes };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateHandSizes(handSizes: number[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, handSizes };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateBattlefields(battlefields: Permanent[][]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, battlefields };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
    // Clear pending combat state now that server has confirmed the battlefield
    if (!this.declaringAttackers) {
      this.selectedAttackerIndices.set(new Set());
    }
    if (!this.declaringBlockers) {
      this.blockerAssignments.set(new Map());
    }
  }

  private updateManaPool(manaPool: Record<string, number>): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, manaPool };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateLifeTotals(lifeTotals: number[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, lifeTotals };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateStack(stack: StackEntry[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, stack };
    this.game.set(updated);
    this.websocketService.currentGame = updated;

    if (stack.length > 0) {
      this.activeTab.set('stack');
    } else {
      this.activeTab.set('log');
    }
  }

  private updateGraveyards(graveyards: Card[][]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, graveyards };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleAvailableAttackers(msg: AvailableAttackersNotification): void {
    this.declaringAttackers = true;
    this.availableAttackerIndices.set(new Set(msg.attackerIndices));
    this.selectedAttackerIndices.set(new Set());
  }

  private handleAvailableBlockers(msg: AvailableBlockersNotification): void {
    this.declaringBlockers = true;
    this.availableBlockerIndices.set(new Set(msg.blockerIndices));
    this.opponentAttackerIndices = msg.attackerIndices;
    this.blockerAssignments.set(new Map());
    this.selectedBlockerIndex = null;
  }

  private handleGameOver(msg: GameOverNotification): void {
    this.gameOverWinner = msg.winnerName;
    this.gameOverWinnerId = msg.winnerId;
    const g = this.game();
    if (!g) return;
    const updated = { ...g, status: GameStatus.FINISHED };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleChooseCardFromHand(msg: ChooseCardFromHandNotification): void {
    this.choosingFromHand = true;
    this.choosableHandIndices.set(new Set(msg.cardIndices));
    this.handChoicePrompt = msg.prompt;
  }

  chooseCardFromHand(index: number): void {
    const g = this.game();
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
    const g = this.game();
    if (!g || !this.choosingFromHand) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: -1
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  private handleChooseColor(msg: ChooseColorNotification): void {
    this.choosingColor = true;
    this.colorChoices = msg.colors;
    this.colorChoicePrompt = msg.prompt;
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

  private handleMayAbilityChoice(msg: MayAbilityNotification): void {
    this.awaitingMayAbility = true;
    this.mayAbilityPrompt = msg.prompt;
  }

  acceptMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.MAY_ABILITY_CHOSEN,
      accepted: true
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
  }

  declineMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.MAY_ABILITY_CHOSEN,
      accepted: false
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
  }

  private handleChoosePermanent(msg: ChoosePermanentNotification): void {
    this.choosingPermanent = true;
    this.choosablePermanentIds.set(new Set(msg.permanentIds));
    this.permanentChoicePrompt = msg.prompt;
  }

  choosePermanent(permanentId: string): void {
    if (!this.choosingPermanent) return;
    if (!this.choosablePermanentIds().has(permanentId)) return;
    this.websocketService.send({
      type: MessageType.PERMANENT_CHOSEN,
      permanentId: permanentId
    });
    this.choosingPermanent = false;
    this.choosablePermanentIds.set(new Set());
    this.permanentChoicePrompt = '';
  }

  private handleChooseMultiplePermanents(msg: ChooseMultiplePermanentsNotification): void {
    this.choosingMultiplePermanents = true;
    this.multiPermanentChoiceIds.set(new Set(msg.permanentIds));
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = msg.maxCount;
    this.multiPermanentChoicePrompt = msg.prompt;
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

  get myPlayerIndex(): number {
    const g = this.game();
    if (!g) return 0;
    return g.playerIds.indexOf(this.websocketService.currentUser?.userId ?? '');
  }

  get opponentPlayerIndex(): number {
    return this.myPlayerIndex === 0 ? 1 : 0;
  }

  get myBattlefield(): Permanent[] {
    return this.game()?.battlefields?.[this.myPlayerIndex] ?? [];
  }

  get opponentBattlefield(): Permanent[] {
    return this.game()?.battlefields?.[this.opponentPlayerIndex] ?? [];
  }

  get myGraveyard(): Card[] {
    return this.game()?.graveyards?.[this.myPlayerIndex] ?? [];
  }

  get opponentGraveyard(): Card[] {
    return this.game()?.graveyards?.[this.opponentPlayerIndex] ?? [];
  }

  playCard(index: number): void {
    const g = this.game();
    if (g && this.isCardPlayable(index)) {
      const card = g.hand[index];
      const hasXCost = card.manaCost?.includes('{X}') ?? false;

      if (hasXCost) {
        this.choosingXValue = true;
        this.xValueCardIndex = index;
        this.xValueCardName = card.name;
        this.xValueInput = 0;
        this.xValueMaximum = this.totalMana - 1; // subtract 1 for the {W} base cost
        return;
      }
      if (card.needsSpellTarget) {
        this.targetingSpell = true;
        this.targetingSpellCardIndex = index;
        this.targetingSpellCardName = card.name;
        return;
      }
      if (card.needsTarget) {
        this.targeting = true;
        this.targetingCardIndex = index;
        this.targetingCardName = card.name;
        this.targetingForAbility = false;
        this.targetingForPlayer = card.targetsPlayer ?? false;
        this.targetingRequiresAttacking = card.requiresAttackingTarget ?? false;
        this.targetingAbilityIndex = -1;
        this.pendingAbilityXValue = null;
        this.targetingAllowedTypes = card.allowedTargetTypes?.length > 0 ? card.allowedTargetTypes : [];
        return;
      }
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetPermanentId: null });
    }
  }

  confirmXValue(): void {
    const g = this.game();
    if (!g) return;

    if (this.targetingForAbility) {
      const perm = this.myBattlefield[this.xValueCardIndex];
      const ability = perm?.card.activatedAbilities[this.targetingAbilityIndex];
      if (ability?.needsTarget) {
        // Store X value and enter targeting mode
        this.pendingAbilityXValue = this.xValueInput;
        this.choosingXValue = false;
        this.targeting = true;
        this.targetingCardIndex = this.xValueCardIndex;
        this.targetingCardName = this.xValueCardName;
        this.targetingForPlayer = ability.targetsPlayer;
        this.targetingAllowedTypes = ability.allowedTargetTypes ?? [];
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

  get damageDistributionRemaining(): number {
    let assigned = 0;
    this.damageAssignments.forEach(v => assigned += v);
    return this.damageDistributionXValue - assigned;
  }

  assignDamage(permanentId: string): void {
    if (!this.distributingDamage || this.damageDistributionRemaining <= 0) return;
    const current = this.damageAssignments.get(permanentId) ?? 0;
    this.damageAssignments.set(permanentId, current + 1);
  }

  unassignDamage(permanentId: string): void {
    if (!this.distributingDamage) return;
    const current = this.damageAssignments.get(permanentId) ?? 0;
    if (current <= 1) {
      this.damageAssignments.delete(permanentId);
    } else {
      this.damageAssignments.set(permanentId, current - 1);
    }
  }

  getDamageAssigned(permanentId: string): number {
    return this.damageAssignments.get(permanentId) ?? 0;
  }

  confirmDamageDistribution(): void {
    if (this.damageDistributionRemaining !== 0) return;
    const assignments: Record<string, number> = {};
    this.damageAssignments.forEach((v, k) => assignments[k] = v);
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.damageDistributionCardIndex,
      xValue: this.damageDistributionXValue,
      damageAssignments: assignments
    });
    this.cancelDamageDistribution();
  }

  cancelDamageDistribution(): void {
    this.distributingDamage = false;
    this.damageDistributionCardIndex = -1;
    this.damageDistributionCardName = '';
    this.damageDistributionXValue = 0;
    this.damageAssignments = new Map();
  }

  selectTarget(permanentId: string): void {
    if (!this.targeting) return;
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
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingCardIndex,
        targetPermanentId: permanentId
      });
    }
    this.targeting = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingRequiresAttacking = false;
    this.targetingAbilityIndex = -1;
    this.targetingAllowedTypes = [];
    this.pendingAbilityXValue = null;
  }

  selectPlayerTarget(playerIndex: number): void {
    if (!this.targeting || !this.targetingForPlayer) return;
    const g = this.game();
    if (!g) return;
    const playerId = g.playerIds[playerIndex];
    if (this.targetingForAbility) {
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: playerId
      });
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingCardIndex,
        targetPermanentId: playerId
      });
    }
    this.targeting = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingForPlayer = false;
    this.targetingRequiresAttacking = false;
    this.targetingAbilityIndex = -1;
    this.targetingAllowedTypes = [];
    this.pendingAbilityXValue = null;
  }

  cancelTargeting(): void {
    this.targeting = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingForPlayer = false;
    this.targetingRequiresAttacking = false;
    this.targetingAbilityIndex = -1;
    this.targetingAllowedTypes = [];
    this.pendingAbilityXValue = null;
  }

  selectSpellTarget(entry: StackEntry): void {
    if (!this.targetingSpell || !entry.isSpell) return;
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.targetingSpellCardIndex,
      targetPermanentId: entry.cardId
    });
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
  }

  cancelSpellTargeting(): void {
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
  }

  handleReorderLibraryCards(msg: ReorderLibraryCardsNotification): void {
    this.reorderingLibrary = true;
    this.reorderAllCards = msg.cards;
    this.reorderAvailableIndices = msg.cards.map((_, i) => i);
    this.reorderOriginalIndices = [];
    this.reorderPrompt = msg.prompt;
  }

  get reorderAvailableCards(): { card: Card; originalIndex: number }[] {
    return this.reorderAvailableIndices.map(i => ({ card: this.reorderAllCards[i], originalIndex: i }));
  }

  get reorderPlacedCards(): { card: Card; originalIndex: number; position: number }[] {
    return this.reorderOriginalIndices.map((origIdx, pos) => ({
      card: this.reorderAllCards[origIdx],
      originalIndex: origIdx,
      position: pos + 1
    }));
  }

  selectReorderCard(originalIndex: number): void {
    this.reorderOriginalIndices = [...this.reorderOriginalIndices, originalIndex];
    this.reorderAvailableIndices = this.reorderAvailableIndices.filter(i => i !== originalIndex);
  }

  undoLastReorderCard(): void {
    if (this.reorderOriginalIndices.length === 0) return;
    const lastIdx = this.reorderOriginalIndices[this.reorderOriginalIndices.length - 1];
    this.reorderOriginalIndices = this.reorderOriginalIndices.slice(0, -1);
    this.reorderAvailableIndices = [...this.reorderAvailableIndices, lastIdx];
  }

  confirmReorder(): void {
    this.websocketService.send({
      type: MessageType.LIBRARY_CARDS_REORDERED,
      cardOrder: this.reorderOriginalIndices
    });
    this.reorderingLibrary = false;
    this.reorderAllCards = [];
    this.reorderAvailableIndices = [];
    this.reorderOriginalIndices = [];
    this.reorderPrompt = '';
  }

  closeRevealHand(): void {
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandPlayerName = '';
  }

  isValidTarget(perm: Permanent): boolean {
    if (this.targetingRequiresAttacking) {
      return this.isPermanentCreature(perm) && perm.attacking;
    }
    if (this.targetingAllowedTypes.length > 0) {
      if (!this.targetingAllowedTypes.some(t => t.toUpperCase() === perm.card.type.toUpperCase())) {
        return false;
      }
      // For enchantment-only targeting (e.g., Aura Graft), only allow auras that are attached
      if (this.targetingAllowedTypes.length === 1 && perm.card.type === 'ENCHANTMENT' && perm.attachedTo == null) {
        return false;
      }
      return true;
    }
    return this.isPermanentCreature(perm);
  }

  get player1DeckSize(): number {
    return this.game()?.deckSizes?.[0] ?? 0;
  }

  get player2DeckSize(): number {
    return this.game()?.deckSizes?.[1] ?? 0;
  }

  get player1HandSize(): number {
    return this.game()?.handSizes?.[0] ?? 0;
  }

  get player2HandSize(): number {
    return this.game()?.handSizes?.[1] ?? 0;
  }

  private handleSelectCardsToBottom(msg: SelectCardsToBottomNotification): void {
    this.selectingBottomCards = true;
    this.bottomCardCount = msg.count;
    this.selectedCardIndices.clear();
  }

  keepHand(): void {
    const g = this.game();
    if (g && !this.selfKept) {
      this.websocketService.send({ type: MessageType.KEEP_HAND });
    }
  }

  takeMulligan(): void {
    const g = this.game();
    if (g && !this.selfKept) {
      this.websocketService.send({ type: MessageType.TAKE_MULLIGAN });
    }
  }

  toggleCardSelection(index: number): void {
    if (this.selectedCardIndices.has(index)) {
      this.selectedCardIndices.delete(index);
    } else if (this.selectedCardIndices.size < this.bottomCardCount) {
      this.selectedCardIndices.add(index);
    }
  }

  isCardSelected(index: number): boolean {
    return this.selectedCardIndices.has(index);
  }

  get canConfirmBottom(): boolean {
    return this.selectedCardIndices.size === this.bottomCardCount;
  }

  confirmBottomCards(): void {
    const g = this.game();
    if (g && this.canConfirmBottom) {
      this.websocketService.send({
        type: MessageType.BOTTOM_CARDS,
        cardIndices: Array.from(this.selectedCardIndices)
      });
      this.selectingBottomCards = false;
      this.selectedCardIndices.clear();
    }
  }

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY });
    }
  }

  tapPermanent(index: number): void {
    const g = this.game();
    if (g && this.canTapPermanent(index)) {
      const perm = this.myBattlefield[index];
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

  isPermanentCreature(perm: Permanent): boolean {
    return perm.card.type === 'CREATURE' || perm.animatedCreature;
  }

  private canUseAbility(perm: Permanent, ability: ActivatedAbilityView): boolean {
    if (ability.requiresTap) {
      if (perm.tapped) return false;
      if (perm.summoningSick && this.isPermanentCreature(perm)) return false;
    }
    return true;
  }

  private activateAbilityAtIndex(permanentIndex: number, abilityIndex: number, perm: Permanent): void {
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
      this.xValueMaximum = this.totalMana - base;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Check for targeting
    if (ability.needsTarget) {
      this.targeting = true;
      this.targetingCardIndex = permanentIndex;
      this.targetingCardName = perm.card.name;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      this.targetingForPlayer = ability.targetsPlayer;
      this.targetingAllowedTypes = ability.allowedTargetTypes ?? [];
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
    const perm = this.myBattlefield[this.abilityChoicePermanentIndex];
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
    const perm = this.myBattlefield[index];
    if (perm == null || !this.hasPriority) return false;
    const abilities = perm.card.activatedAbilities;
    // Has a non-tap activated ability (mana-cost only) — always usable
    if (abilities.some(a => !a.requiresTap)) return true;
    if (perm.tapped) return false;
    if (!perm.card.hasTapAbility) return false;
    if (perm.summoningSick && this.isPermanentCreature(perm)) return false;
    return true;
  }

  get manaPool(): Record<string, number> {
    return this.game()?.manaPool ?? {};
  }

  get totalMana(): number {
    return Object.values(this.manaPool).reduce((sum, v) => sum + v, 0);
  }

  get manaEntries(): { color: string; count: number }[] {
    return Object.entries(this.manaPool)
      .filter(([, count]) => count > 0)
      .map(([color, count]) => ({ color, count }));
  }

  opponentKept = false;
  selfKept = false;
  selectingBottomCards = false;
  bottomCardCount = 0;
  selectedCardIndices = new Set<number>();
  playableCardIndices = signal(new Set<number>());
  autoStopSteps = signal(new Set<string>());

  // Combat state
  declaringAttackers = false;
  declaringBlockers = false;
  availableAttackerIndices = signal(new Set<number>());
  availableBlockerIndices = signal(new Set<number>());
  selectedAttackerIndices = signal(new Set<number>());
  opponentAttackerIndices: number[] = [];
  blockerAssignments = signal(new Map<number, number>());
  selectedBlockerIndex: number | null = null;
  gameOverWinner: string | null = null;
  gameOverWinnerId: string | null = null;
  choosingFromHand = false;
  choosableHandIndices = signal(new Set<number>());
  handChoicePrompt = '';
  choosingColor = false;
  colorChoices: string[] = [];
  colorChoicePrompt = '';
  awaitingMayAbility = false;
  mayAbilityPrompt = '';
  choosingPermanent = false;
  choosablePermanentIds = signal(new Set<string>());
  permanentChoicePrompt = '';
  choosingMultiplePermanents = false;
  multiPermanentChoiceIds = signal(new Set<string>());
  multiPermanentSelectedIds = signal(new Set<string>());
  multiPermanentMaxCount = 0;
  multiPermanentChoicePrompt = '';

  // Ability picker state
  choosingAbility = false;
  abilityChoicePermanentIndex = -1;
  abilityChoices: { ability: ActivatedAbilityView; index: number; usable: boolean }[] = [];

  // Targeting state (for instants and activated abilities)
  targeting = false;
  targetingCardIndex = -1;
  targetingCardName = '';
  targetingForAbility = false;
  targetingForPlayer = false;
  targetingAllowedTypes: string[] = [];
  targetingRequiresAttacking = false;
  targetingAbilityIndex = -1;
  pendingAbilityXValue: number | null = null;

  // Spell targeting state (for counterspells)
  targetingSpell = false;
  targetingSpellCardIndex = -1;
  targetingSpellCardName = '';

  // Library reorder state
  reorderingLibrary = false;
  reorderAllCards: Card[] = [];
  reorderAvailableIndices: number[] = [];
  reorderOriginalIndices: number[] = [];
  reorderPrompt = '';

  // Reveal hand state
  revealingHand = false;
  revealedHandCards: Card[] = [];
  revealedHandPlayerName = '';

  // X cost prompt state
  choosingXValue = false;
  xValueCardIndex = -1;
  xValueCardName = '';
  xValueInput = 0;
  xValueMaximum = 0;

  // Damage distribution state
  distributingDamage = false;
  damageDistributionCardIndex = -1;
  damageDistributionCardName = '';
  damageDistributionXValue = 0;
  damageAssignments: Map<string, number> = new Map();

  isCardPlayable(index: number): boolean {
    return this.playableCardIndices().has(index);
  }

  toggleAutoStop(step: string): void {
    if (this.isForceStop(step)) return;

    const g = this.game();
    if (!g) return;

    const current = new Set(this.autoStopSteps());
    if (current.has(step)) {
      current.delete(step);
    } else {
      current.add(step);
    }
    this.autoStopSteps.set(current);

    this.websocketService.send({
      type: MessageType.SET_AUTO_STOPS,
      stops: Array.from(current)
    });
  }

  isAutoStop(step: string): boolean {
    return this.autoStopSteps().has(step);
  }

  isForceStop(step: string): boolean {
    return step === TurnStep.PRECOMBAT_MAIN || step === TurnStep.POSTCOMBAT_MAIN;
  }

  // Combat action methods

  canAttack(index: number): boolean {
    return this.declaringAttackers && this.availableAttackerIndices().has(index);
  }

  isSelectedAttacker(index: number): boolean {
    return this.selectedAttackerIndices().has(index);
  }

  toggleAttacker(index: number): void {
    if (!this.canAttack(index)) return;
    const updated = new Set(this.selectedAttackerIndices());
    if (updated.has(index)) {
      updated.delete(index);
    } else {
      updated.add(index);
    }
    this.selectedAttackerIndices.set(updated);
  }

  confirmAttackers(): void {
    const g = this.game();
    if (!g) return;
    this.websocketService.send({
      type: MessageType.DECLARE_ATTACKERS,
      attackerIndices: Array.from(this.selectedAttackerIndices())
    });
    this.declaringAttackers = false;
    // Keep selectedAttackerIndices populated until server responds with battlefield update
    this.availableAttackerIndices.set(new Set());
  }

  canBlock(index: number): boolean {
    return this.declaringBlockers && this.availableBlockerIndices().has(index);
  }

  isAssignedBlocker(index: number): boolean {
    return this.blockerAssignments().has(index);
  }

  selectBlocker(index: number): void {
    if (!this.canBlock(index)) return;
    if (this.blockerAssignments().has(index)) {
      const updated = new Map(this.blockerAssignments());
      updated.delete(index);
      this.blockerAssignments.set(updated);
      return;
    }
    this.selectedBlockerIndex = index;
  }

  isBlockTarget(index: number): boolean {
    return this.declaringBlockers && this.selectedBlockerIndex !== null
      && this.opponentBattlefield[index]?.attacking === true;
  }

  assignBlock(attackerIndex: number): void {
    if (this.selectedBlockerIndex === null || !this.declaringBlockers) return;
    const perm = this.opponentBattlefield[attackerIndex];
    if (!perm || !perm.attacking) return;
    const updated = new Map(this.blockerAssignments());
    updated.set(this.selectedBlockerIndex, attackerIndex);
    this.blockerAssignments.set(updated);
    this.selectedBlockerIndex = null;
  }

  confirmBlockers(): void {
    const g = this.game();
    if (!g) return;
    const assignments = Array.from(this.blockerAssignments().entries()).map(([blockerIndex, attackerIndex]) => ({
      blockerIndex,
      attackerIndex
    }));
    this.websocketService.send({
      type: MessageType.DECLARE_BLOCKERS,
      blockerAssignments: assignments
    });
    this.declaringBlockers = false;
    // Keep blockerAssignments populated until server responds with battlefield update
    this.selectedBlockerIndex = null;
    this.availableBlockerIndices.set(new Set());
    this.opponentAttackerIndices = [];
  }

  cancelBlockerSelection(): void {
    this.selectedBlockerIndex = null;
  }

  get myLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.myPlayerIndex] ?? 20;
  }

  get opponentLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.opponentPlayerIndex] ?? 20;
  }

  get stackEntries(): StackEntry[] {
    return [...(this.game()?.stack ?? [])].reverse();
  }

  get isStackEmpty(): boolean {
    return (this.game()?.stack ?? []).length === 0;
  }

  getPlayerName(playerId: string): string {
    const g = this.game();
    if (!g) return '';
    const idx = g.playerIds.indexOf(playerId);
    return idx >= 0 ? g.playerNames[idx] : '';
  }

  getLifeTotal(playerIndex: number): number {
    return this.game()?.lifeTotals?.[playerIndex] ?? 20;
  }

  onMyBattlefieldCardClick(index: number): void {
    if (this.choosingPermanent) {
      const perm = this.myBattlefield[index];
      if (perm && this.choosablePermanentIds().has(perm.id)) {
        this.choosePermanent(perm.id);
      }
      return;
    }
    if (this.choosingMultiplePermanents) {
      const perm = this.myBattlefield[index];
      if (perm && this.multiPermanentChoiceIds().has(perm.id)) {
        this.toggleMultiPermanentSelection(perm.id);
      }
      return;
    }
    if (this.distributingDamage) {
      const perm = this.myBattlefield[index];
      if (perm && this.isPermanentCreature(perm) && perm.attacking) {
        this.assignDamage(perm.id);
      }
      return;
    }
    if (this.targeting) {
      const perm = this.myBattlefield[index];
      if (perm && this.isValidTarget(perm)) {
        this.selectTarget(perm.id);
      }
      return;
    }
    if (this.declaringAttackers) {
      this.toggleAttacker(index);
    } else if (this.declaringBlockers) {
      this.selectBlocker(index);
    } else {
      this.tapPermanent(index);
    }
  }

  onOpponentBattlefieldCardClick(index: number): void {
    if (this.choosingPermanent) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.choosablePermanentIds().has(perm.id)) {
        this.choosePermanent(perm.id);
      }
      return;
    }
    if (this.choosingMultiplePermanents) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.multiPermanentChoiceIds().has(perm.id)) {
        this.toggleMultiPermanentSelection(perm.id);
      }
      return;
    }
    if (this.distributingDamage) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.isPermanentCreature(perm) && perm.attacking) {
        this.assignDamage(perm.id);
      }
      return;
    }
    if (this.targeting) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.isValidTarget(perm)) {
        this.selectTarget(perm.id);
      }
      return;
    }
    if (this.declaringBlockers) {
      this.assignBlock(index);
    }
  }

  backToLobby(): void {
    this.router.navigate(['/home']);
  }

  onCardHover(card: Card, permanent: Permanent | null = null): void {
    this.hoveredCard.set(card);
    this.hoveredPermanent.set(permanent);
  }

  onCardHoverEnd(): void {
    this.hoveredCard.set(null);
    this.hoveredPermanent.set(null);
  }

  onStackEntryHover(entry: StackEntry): void {
    this.stackTargetId.set(entry.targetPermanentId);
  }

  onStackEntryHoverEnd(): void {
    this.stackTargetId.set(null);
  }

  isStackTargetPlayer(playerIndex: number): boolean {
    const g = this.game();
    if (!g) return false;
    return this.stackTargetId() === g.playerIds[playerIndex];
  }

  isStackTargetSpell(entry: StackEntry): boolean {
    return this.stackTargetId() === entry.cardId;
  }

  getStackEntryTargetName(entry: StackEntry): string | null {
    if (!entry.targetPermanentId) return null;
    const g = this.game();
    if (!g) return null;
    // Check if targeting a player
    const playerIdx = g.playerIds.indexOf(entry.targetPermanentId);
    if (playerIdx >= 0) return g.playerNames[playerIdx];
    // Check if targeting a permanent on battlefield
    for (const bf of g.battlefields) {
      for (const perm of bf) {
        if (perm.id === entry.targetPermanentId) return perm.card.name;
      }
    }
    // Check if targeting a spell on the stack
    for (const se of g.stack) {
      if (se.cardId === entry.targetPermanentId) return se.card.name;
    }
    return null;
  }

  // Battlefield splitting: lands (back row) vs creatures (front row)

  private splitBattlefield(battlefield: Permanent[]): { lands: IndexedPermanent[], creatures: IndexedPermanent[] } {
    const lands: IndexedPermanent[] = [];
    const creatures: IndexedPermanent[] = [];
    battlefield.forEach((perm, idx) => {
      if (perm.attachedTo != null) return; // Auras rendered with their host
      const entry: IndexedPermanent = { perm, originalIndex: idx };
      if (perm.card.type === 'CREATURE' || perm.animatedCreature) {
        creatures.push(entry);
      } else {
        lands.push(entry);
      }
    });
    return { lands, creatures };
  }

  getAttachedAuras(permanentId: string): AttachedAura[] {
    const auras: AttachedAura[] = [];
    this.myBattlefield.forEach((perm, idx) => {
      if (perm.attachedTo === permanentId) {
        auras.push({ perm, originalIndex: idx, isMine: true });
      }
    });
    this.opponentBattlefield.forEach((perm, idx) => {
      if (perm.attachedTo === permanentId) {
        auras.push({ perm, originalIndex: idx, isMine: false });
      }
    });
    return auras;
  }

  onAuraClick(aura: AttachedAura): void {
    if (aura.isMine) {
      this.onMyBattlefieldCardClick(aura.originalIndex);
    } else {
      this.onOpponentBattlefieldCardClick(aura.originalIndex);
    }
  }

  get myLands(): IndexedPermanent[] {
    return this.splitBattlefield(this.myBattlefield).lands;
  }

  get opponentLands(): IndexedPermanent[] {
    return this.splitBattlefield(this.opponentBattlefield).lands;
  }

  private stackBasicLands(lands: IndexedPermanent[]): (IndexedPermanent | LandStack)[] {
    const MAX_STACK = 4;
    const result: (IndexedPermanent | LandStack)[] = [];
    const basicGroups = new Map<string, IndexedPermanent[]>();
    const nonBasic: IndexedPermanent[] = [];

    for (const ip of lands) {
      if (ip.perm.card.type === 'BASIC_LAND') {
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

  get myLandStacks(): (IndexedPermanent | LandStack)[] {
    return this.stackBasicLands(this.myLands);
  }

  get opponentLandStacks(): (IndexedPermanent | LandStack)[] {
    return this.stackBasicLands(this.opponentLands);
  }

  isLandStack(item: IndexedPermanent | LandStack): item is LandStack {
    return 'lands' in item;
  }

  myCreaturesNotInCombat = computed(() => {
    const selectedAttackers = this.selectedAttackerIndices();
    const assignedBlockers = this.blockerAssignments();
    return this.splitBattlefield(this.myBattlefield).creatures.filter(c => {
      if (selectedAttackers.has(c.originalIndex)) return false;
      if (assignedBlockers.has(c.originalIndex)) return false;
      if (c.perm.attacking || c.perm.blocking) return false;
      return true;
    });
  });

  opponentCreaturesNotInCombat = computed(() => {
    return this.splitBattlefield(this.opponentBattlefield).creatures.filter(c => {
      if (c.perm.attacking || c.perm.blocking) return false;
      return true;
    });
  });

  // Combat zone: groups of attacker + blockers

  get showCombatZone(): boolean {
    return this.combatPairings.length > 0;
  }

  get combatPairings(): CombatGroup[] {
    const groups: CombatGroup[] = [];

    // During declaring attackers (or pending server confirmation): selected attackers move to combat zone
    const selectedAttackers = this.selectedAttackerIndices();
    if (this.declaringAttackers || selectedAttackers.size > 0) {
      for (const idx of selectedAttackers) {
        groups.push({
          attackerIndex: idx,
          attacker: this.myBattlefield[idx],
          attackerIsMine: true,
          blockers: []
        });
      }
      return groups;
    }

    // Server-confirmed attacking creatures
    const myBf = this.myBattlefield;
    const oppBf = this.opponentBattlefield;
    const myAttacking = myBf.some(p => p.attacking);
    const oppAttacking = oppBf.some(p => p.attacking);

    if (myAttacking) {
      // I'm the attacker
      myBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: true, blockers: [] };
        // Opponent's blockers targeting this attacker
        oppBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTargets.includes(idx)) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: false });
          }
        });
        groups.push(group);
      });
    } else if (oppAttacking) {
      // Opponent is the attacker
      oppBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: false, blockers: [] };
        // My server-confirmed blockers
        myBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTargets.includes(idx)) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: true });
          }
        });
        // My locally assigned blockers (during declaration or pending server confirmation)
        const assignments = this.blockerAssignments();
        if (this.declaringBlockers || assignments.size > 0) {
          for (const [blockerIdx, atkIdx] of assignments) {
            if (atkIdx === idx) {
              const alreadyIncluded = group.blockers.some(b => b.index === blockerIdx && b.isMine);
              if (!alreadyIncluded) {
                group.blockers.push({ index: blockerIdx, perm: myBf[blockerIdx], isMine: true });
              }
            }
          }
        }
        groups.push(group);
      });
    }

    return groups;
  }

  private get myIndicesInCombat(): Set<number> {
    const set = new Set<number>();
    for (const group of this.combatPairings) {
      if (group.attackerIsMine) set.add(group.attackerIndex);
      for (const b of group.blockers) {
        if (b.isMine) set.add(b.index);
      }
    }
    return set;
  }

  private get opponentIndicesInCombat(): Set<number> {
    const set = new Set<number>();
    for (const group of this.combatPairings) {
      if (!group.attackerIsMine) set.add(group.attackerIndex);
      for (const b of group.blockers) {
        if (!b.isMine) set.add(b.index);
      }
    }
    return set;
  }

  onCombatAttackerClick(group: CombatGroup): void {
    if (this.targeting) {
      if (this.isValidTarget(group.attacker)) {
        this.selectTarget(group.attacker.id);
      }
      return;
    }
    if (this.distributingDamage) {
      this.assignDamage(group.attacker.id);
      return;
    }
    if (this.declaringAttackers && group.attackerIsMine) {
      this.toggleAttacker(group.attackerIndex);
    } else if (this.declaringBlockers && !group.attackerIsMine) {
      this.assignBlock(group.attackerIndex);
    }
  }

  onCombatBlockerClick(blocker: CombatBlocker): void {
    if (this.declaringBlockers && blocker.isMine) {
      this.selectBlocker(blocker.index);
    }
  }

  formatTypeLine(card: Card): string {
    const supertypes = (card.supertypes ?? []).map(s => s.charAt(0) + s.slice(1).toLowerCase());
    return [...supertypes, card.type].join(' ');
  }

  formatKeywords(keywords: string[]): string {
    return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase().replace('_', ' ')).join(', ');
  }

  getEffectiveKeywords(perm: Permanent): string[] {
    if (perm.grantedKeywords) {
      return perm.grantedKeywords.filter(kw => !perm.card.keywords.includes(kw));
    }
    return [];
  }

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
