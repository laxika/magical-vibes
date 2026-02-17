import { Component, OnInit, OnDestroy, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameStateNotification, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, Permanent, MulliganResolvedNotification, SelectCardsToBottomNotification, AvailableAttackersNotification, AvailableBlockersNotification, GameOverNotification, ChooseCardFromHandNotification, ChooseColorNotification, MayAbilityNotification, ChoosePermanentNotification, ChooseMultiplePermanentsNotification, ChooseMultipleCardsFromGraveyardsNotification, StackEntry, ReorderLibraryCardsNotification, ChooseCardFromLibraryNotification, RevealHandNotification, ChooseFromRevealedHandNotification, ChooseCardFromGraveyardNotification, ChooseHandTopBottomNotification } from '../../services/websocket.service';
import { GameChoiceService } from '../../services/game-choice.service';
import { CardDisplayComponent } from './card-display/card-display.component';
import { IndexedPermanent, CombatGroup, CombatBlocker, AttachedAura, LandStack, splitBattlefield, stackBasicLands, getAttachedAuras, isLandStack, isPermanentCreature } from './battlefield.utils';
import { Subscription } from 'rxjs';

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
  activeTab = signal<'game' | 'stack' | 'graveyard'>('game');
  private subscriptions: Subscription[] = [];

  readonly choice = inject(GameChoiceService);

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

    this.choice.init(
      this.game,
      () => this.myBattlefield,
      () => this.opponentBattlefield,
      () => this.totalMana
    );

    const initialStops = this.websocketService.currentGame?.autoStopSteps;
    if (initialStops) {
      this.autoStopSteps.set(new Set(initialStops));
    }

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        console.log(message);

        if (message.type === MessageType.OPPONENT_JOINED) {
          const notification = message as GameNotification;
          if (notification.game) {
            this.game.set(notification.game);
            this.websocketService.currentGame = notification.game;
          }
        }

        if (message.type === MessageType.GAME_STATE) {
          this.applyGameState(message as GameStateNotification);
        }

        if (message.type === MessageType.MULLIGAN_RESOLVED) {
          this.handleMulliganResolved(message as MulliganResolvedNotification);
        }

        if (message.type === MessageType.SELECT_CARDS_TO_BOTTOM) {
          this.handleSelectCardsToBottom(message as SelectCardsToBottomNotification);
        }

        if (message.type === MessageType.AVAILABLE_ATTACKERS) {
          this.handleAvailableAttackers(message as AvailableAttackersNotification);
        }

        if (message.type === MessageType.AVAILABLE_BLOCKERS) {
          this.handleAvailableBlockers(message as AvailableBlockersNotification);
        }

        if (message.type === MessageType.GAME_OVER) {
          this.handleGameOver(message as GameOverNotification);
        }

        if (message.type === MessageType.CHOOSE_CARD_FROM_HAND) {
          this.choice.handleChooseCardFromHand(message as ChooseCardFromHandNotification);
        }

        if (message.type === MessageType.CHOOSE_COLOR) {
          this.choice.handleChooseColor(message as ChooseColorNotification);
        }

        if (message.type === MessageType.MAY_ABILITY_CHOICE) {
          this.choice.handleMayAbilityChoice(message as MayAbilityNotification);
        }

        if (message.type === MessageType.CHOOSE_PERMANENT) {
          this.choice.handleChoosePermanent(message as ChoosePermanentNotification);
        }

        if (message.type === MessageType.CHOOSE_MULTIPLE_PERMANENTS) {
          this.choice.handleChooseMultiplePermanents(message as ChooseMultiplePermanentsNotification);
        }

        if (message.type === MessageType.CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS) {
          this.choice.handleChooseMultipleCardsFromGraveyards(message as ChooseMultipleCardsFromGraveyardsNotification);
        }

        if (message.type === MessageType.REORDER_LIBRARY_CARDS) {
          this.choice.handleReorderLibraryCards(message as ReorderLibraryCardsNotification);
        }

        if (message.type === MessageType.CHOOSE_CARD_FROM_LIBRARY) {
          this.choice.handleChooseCardFromLibrary(message as ChooseCardFromLibraryNotification);
        }

        if (message.type === MessageType.CHOOSE_HAND_TOP_BOTTOM) {
          this.choice.handleChooseHandTopBottom(message as ChooseHandTopBottomNotification);
        }

        if (message.type === MessageType.REVEAL_HAND) {
          this.choice.handleRevealHand(message as RevealHandNotification);
        }

        if (message.type === MessageType.CHOOSE_FROM_REVEALED_HAND) {
          this.choice.handleChooseFromRevealedHand(message as ChooseFromRevealedHandNotification);
        }

        if (message.type === MessageType.CHOOSE_CARD_FROM_GRAVEYARD) {
          this.choice.handleChooseCardFromGraveyard(message as ChooseCardFromGraveyardNotification);
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

  // ========== Player info getters ==========

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

  get opponentHand(): Card[] {
    return this.game()?.opponentHand ?? [];
  }

  get hand(): Card[] {
    return this.game()?.hand ?? [];
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

  get myLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.myPlayerIndex] ?? 20;
  }

  get opponentLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.opponentPlayerIndex] ?? 20;
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

  getPlayerId(playerIndex: number): string {
    return this.game()?.playerIds?.[playerIndex] ?? '';
  }

  // ========== Game state ==========

  private applyGameState(state: GameStateNotification): void {
    const g = this.game();
    if (!g) return;

    // Detect transition to RUNNING to clear mulligan UI state
    if (state.status === GameStatus.RUNNING && g.status !== GameStatus.RUNNING) {
      this.opponentKept = false;
      this.selfKept = false;
      this.selectingBottomCards = false;
      this.bottomCardCount = 0;
      this.selectedCardIndices.clear();
    }

    const updated = {
      ...g,
      status: state.status,
      activePlayerId: state.activePlayerId,
      turnNumber: state.turnNumber,
      currentStep: state.currentStep,
      priorityPlayerId: state.priorityPlayerId,
      battlefields: state.battlefields,
      stack: state.stack,
      graveyards: state.graveyards,
      deckSizes: state.deckSizes,
      handSizes: state.handSizes,
      lifeTotals: state.lifeTotals,
      hand: state.hand,
      opponentHand: state.opponentHand ?? [],
      mulliganCount: state.mulliganCount,
      manaPool: state.manaPool,
      autoStopSteps: state.autoStopSteps,
      gameLog: [...g.gameLog, ...state.newLogEntries]
    };
    this.game.set(updated);
    this.websocketService.currentGame = updated;

    this.playableCardIndices.set(new Set(state.playableCardIndices));
    this.playableGraveyardLandIndices.set(new Set(state.playableGraveyardLandIndices ?? []));
    this.autoStopSteps.set(new Set(state.autoStopSteps));

    // Switch to stack tab when stack is non-empty
    if (state.stack.length > 0) {
      this.activeTab.set('stack');
    } else if (this.activeTab() === 'stack') {
      this.activeTab.set('game');
    }

    // Clear pending combat state when server confirms battlefield
    if (!this.declaringAttackers) {
      this.selectedAttackerIndices.set(new Set());
    }
    if (!this.declaringBlockers) {
      this.blockerAssignments.set(new Map());
    }
  }

  // ========== Mulligan ==========

  opponentKept = false;
  selfKept = false;
  selectingBottomCards = false;
  bottomCardCount = 0;
  selectedCardIndices = new Set<number>();

  get isMulliganPhase(): boolean {
    const g = this.game();
    return g !== null && g.status === GameStatus.MULLIGAN;
  }

  get canMulligan(): boolean {
    const g = this.game();
    return g !== null && g.mulliganCount < 7;
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

  // ========== Priority & playability ==========

  playableCardIndices = signal(new Set<number>());
  playableGraveyardLandIndices = signal(new Set<number>());
  autoStopSteps = signal(new Set<string>());

  isCardPlayable(index: number): boolean {
    return this.playableCardIndices().has(index);
  }

  isGraveyardLandPlayable(index: number): boolean {
    return this.playableGraveyardLandIndices().has(index);
  }

  playCard(index: number): void {
    this.choice.playCard(index, (i) => this.isCardPlayable(i));
  }

  playGraveyardLand(index: number): void {
    if (this.isGraveyardLandPlayable(index)) {
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetPermanentId: null, fromGraveyard: true });
    }
  }

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY });
    }
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

  // ========== Combat ==========

  declaringAttackers = false;
  declaringBlockers = false;
  availableAttackerIndices = signal(new Set<number>());
  mustAttackIndices = signal(new Set<number>());
  availableBlockerIndices = signal(new Set<number>());
  selectedAttackerIndices = signal(new Set<number>());
  opponentAttackerIndices: number[] = [];
  blockerAssignments = signal(new Map<number, number>());
  selectedBlockerIndex: number | null = null;
  gameOverWinner: string | null = null;
  gameOverWinnerId: string | null = null;

  private handleAvailableAttackers(msg: AvailableAttackersNotification): void {
    this.declaringAttackers = true;
    this.availableAttackerIndices.set(new Set(msg.attackerIndices));
    this.mustAttackIndices.set(new Set(msg.mustAttackIndices));
    this.selectedAttackerIndices.set(new Set(msg.mustAttackIndices));
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
      if (this.mustAttackIndices().has(index)) return;
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
    this.availableAttackerIndices.set(new Set());
    this.mustAttackIndices.set(new Set());
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
    this.selectedBlockerIndex = null;
    this.availableBlockerIndices.set(new Set());
    this.opponentAttackerIndices = [];
  }

  cancelBlockerSelection(): void {
    this.selectedBlockerIndex = null;
  }

  // ========== Battlefield display ==========

  getAttachedAuras(permanentId: string): AttachedAura[] {
    return getAttachedAuras(permanentId, this.myBattlefield, this.opponentBattlefield);
  }

  onAuraClick(aura: AttachedAura): void {
    if (aura.isMine) {
      this.onMyBattlefieldCardClick(aura.originalIndex);
    } else {
      this.onOpponentBattlefieldCardClick(aura.originalIndex);
    }
  }

  get myLands(): IndexedPermanent[] {
    return splitBattlefield(this.myBattlefield).lands;
  }

  get opponentLands(): IndexedPermanent[] {
    return splitBattlefield(this.opponentBattlefield).lands;
  }

  get myLandStacks(): (IndexedPermanent | LandStack)[] {
    return stackBasicLands(this.myLands);
  }

  get opponentLandStacks(): (IndexedPermanent | LandStack)[] {
    return stackBasicLands(this.opponentLands);
  }

  isLandStack(item: IndexedPermanent | LandStack): item is LandStack {
    return isLandStack(item);
  }

  isPermanentCreature(perm: Permanent): boolean {
    return isPermanentCreature(perm);
  }

  myCreaturesNotInCombat = computed(() => {
    const selectedAttackers = this.selectedAttackerIndices();
    const assignedBlockers = this.blockerAssignments();
    return splitBattlefield(this.myBattlefield).creatures.filter(c => {
      if (selectedAttackers.has(c.originalIndex)) return false;
      if (assignedBlockers.has(c.originalIndex)) return false;
      if (c.perm.attacking || c.perm.blocking) return false;
      return true;
    });
  });

  opponentCreaturesNotInCombat = computed(() => {
    return splitBattlefield(this.opponentBattlefield).creatures.filter(c => {
      if (c.perm.attacking || c.perm.blocking) return false;
      return true;
    });
  });

  // ========== Combat zone ==========

  get showCombatZone(): boolean {
    return this.combatPairings.length > 0;
  }

  get combatPairings(): CombatGroup[] {
    const groups: CombatGroup[] = [];

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

    const myBf = this.myBattlefield;
    const oppBf = this.opponentBattlefield;
    const myAttacking = myBf.some(p => p.attacking);
    const oppAttacking = oppBf.some(p => p.attacking);

    if (myAttacking) {
      myBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: true, blockers: [] };
        oppBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTargets.includes(idx)) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: false });
          }
        });
        groups.push(group);
      });
    } else if (oppAttacking) {
      oppBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: false, blockers: [] };
        myBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTargets.includes(idx)) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: true });
          }
        });
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

  // ========== Click dispatch ==========

  onMyBattlefieldCardClick(index: number): void {
    if (this.choice.choosingPermanent) {
      const perm = this.myBattlefield[index];
      if (perm && this.choice.choosablePermanentIds().has(perm.id)) {
        this.choice.choosePermanent(perm.id);
      }
      return;
    }
    if (this.choice.choosingMultiplePermanents) {
      const perm = this.myBattlefield[index];
      if (perm && this.choice.multiPermanentChoiceIds().has(perm.id)) {
        this.choice.toggleMultiPermanentSelection(perm.id);
      }
      return;
    }
    if (this.choice.multiTargeting) {
      const perm = this.myBattlefield[index];
      if (perm && isPermanentCreature(perm)) {
        if (this.choice.isMultiTargetSelected(perm.id)) {
          this.choice.removeMultiTarget(perm.id);
        } else {
          this.choice.addMultiTarget(perm.id);
        }
      }
      return;
    }
    if (this.choice.convoking) {
      const perm = this.myBattlefield[index];
      if (perm && isPermanentCreature(perm) && !perm.tapped) {
        this.choice.toggleConvokeCreature(perm.id);
      }
      return;
    }
    if (this.choice.distributingDamage) {
      const perm = this.myBattlefield[index];
      if (perm && isPermanentCreature(perm) && perm.attacking) {
        this.choice.assignDamage(perm.id);
      }
      return;
    }
    if (this.choice.targeting) {
      const perm = this.myBattlefield[index];
      if (perm && this.choice.isValidTarget(perm)) {
        this.choice.selectTarget(perm.id);
      }
      return;
    }
    if (this.declaringAttackers) {
      this.toggleAttacker(index);
    } else if (this.declaringBlockers) {
      this.selectBlocker(index);
    } else {
      this.choice.tapPermanent(index);
    }
  }

  onOpponentBattlefieldCardClick(index: number): void {
    if (this.choice.choosingPermanent) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.choice.choosablePermanentIds().has(perm.id)) {
        this.choice.choosePermanent(perm.id);
      }
      return;
    }
    if (this.choice.choosingMultiplePermanents) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.choice.multiPermanentChoiceIds().has(perm.id)) {
        this.choice.toggleMultiPermanentSelection(perm.id);
      }
      return;
    }
    if (this.choice.multiTargeting) {
      const perm = this.opponentBattlefield[index];
      if (perm && isPermanentCreature(perm)) {
        if (this.choice.isMultiTargetSelected(perm.id)) {
          this.choice.removeMultiTarget(perm.id);
        } else {
          this.choice.addMultiTarget(perm.id);
        }
      }
      return;
    }
    if (this.choice.distributingDamage) {
      const perm = this.opponentBattlefield[index];
      if (perm && isPermanentCreature(perm) && perm.attacking) {
        this.choice.assignDamage(perm.id);
      }
      return;
    }
    if (this.choice.targeting) {
      const perm = this.opponentBattlefield[index];
      if (perm && this.choice.isValidTarget(perm)) {
        this.choice.selectTarget(perm.id);
      }
      return;
    }
    if (this.declaringBlockers) {
      this.assignBlock(index);
    }
  }

  onCombatAttackerClick(group: CombatGroup): void {
    if (this.choice.multiTargeting) {
      if (isPermanentCreature(group.attacker)) {
        if (this.choice.isMultiTargetSelected(group.attacker.id)) {
          this.choice.removeMultiTarget(group.attacker.id);
        } else {
          this.choice.addMultiTarget(group.attacker.id);
        }
      }
      return;
    }
    if (this.choice.targeting) {
      if (this.choice.isValidTarget(group.attacker)) {
        this.choice.selectTarget(group.attacker.id);
      }
      return;
    }
    if (this.choice.distributingDamage) {
      this.choice.assignDamage(group.attacker.id);
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

  // ========== Stack display ==========

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

  onPlayerBadgeClick(playerIndex: number): void {
    const playerId = this.getPlayerId(playerIndex);
    if (this.choice.choosingPermanent && this.choice.choosablePermanentIds().has(playerId)) {
      this.choice.choosePermanent(playerId);
    } else if (this.choice.multiTargeting && this.choice.multiTargetForPlayer) {
      if (this.choice.isMultiTargetSelected(playerId)) {
        this.choice.removeMultiTarget(playerId);
      } else {
        this.choice.addMultiTargetPlayer(playerIndex);
      }
    } else {
      this.choice.selectPlayerTarget(playerIndex);
    }
  }

  isStackTargetSpell(entry: StackEntry): boolean {
    return this.stackTargetId() === entry.cardId;
  }

  getStackEntryTargetName(entry: StackEntry): string | null {
    if (!entry.targetPermanentId) return null;
    const g = this.game();
    if (!g) return null;
    const playerIdx = g.playerIds.indexOf(entry.targetPermanentId);
    if (playerIdx >= 0) return g.playerNames[playerIdx];
    for (const bf of g.battlefields) {
      for (const perm of bf) {
        if (perm.id === entry.targetPermanentId) return perm.card.name;
      }
    }
    for (const se of g.stack) {
      if (se.cardId === entry.targetPermanentId) return se.card.name;
    }
    return null;
  }

  // ========== Hover & navigation ==========

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

  // ========== Formatting ==========

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
