import { Component, AfterViewInit, OnDestroy, ViewChild, signal, computed, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Game, Card, Permanent, TurnStep, PHASE_GROUPS } from '../../services/websocket.service';
import { CardDisplayComponent } from '../game/card-display/card-display.component';
import { SidePanelComponent } from '../game/side-panel/side-panel.component';
import { IndexedPermanent, LandStack, splitBattlefield, stackBasicLands, isLandStack } from '../game/battlefield.utils';
import { BattlefieldFitModel, BattlefieldSide } from '../game/battlefield-zoom';
import { PHONE_LAYOUT_MEDIA } from '../../services/card-preview.service';
import { TUTORIAL_MOCK_GAME } from './tutorial-mock-data';
import { TUTORIAL_STEPS, TutorialStep } from './tutorial-steps';

/** Template context for the single #permanentStack definition in tutorial.component.html. */
export interface TutorialStackContext {
  $implicit: IndexedPermanent;
  mine: boolean;
}

/** Container-relative rectangle (not viewport-relative). */
interface AbsoluteRect {
  top: number;
  left: number;
  width: number;
  height: number;
  right: number;
  bottom: number;
  /** Viewport-relative centre, used to pick which screen edge the phone sheet docks to. */
  viewportMidY: number;
}

@Component({
  selector: 'app-tutorial',
  standalone: true,
  imports: [CommonModule, CardDisplayComponent, SidePanelComponent],
  templateUrl: './tutorial.component.html',
  styleUrls: ['../game/shared-game-styles.css', '../game/game-phone.css', './tutorial.component.css']
})
export class TutorialComponent implements AfterViewInit, OnDestroy {
  game = signal<Game>(structuredClone(TUTORIAL_MOCK_GAME));
  currentStepIndex = signal(0);
  hoveredCard = signal<Card | null>(null);
  hoveredPermanent = signal<Permanent | null>(null);
  spotlightRect = signal<AbsoluteRect | null>(null);

  readonly steps = TUTORIAL_STEPS;
  readonly phaseGroups = PHASE_GROUPS;
  readonly TurnStep = TurnStep;

  // No-op bound functions for SidePanelComponent callback inputs
  readonly boundIsGraveyardLandPlayable = () => false;
  readonly boundIsGraveyardAbilityActivatable = () => false;
  readonly boundIsFlashbackPlayable = () => false;
  readonly boundGetPlayerName = (playerId: string) => {
    const g = this.game();
    const idx = g.playerIds.indexOf(playerId);
    return idx >= 0 ? g.playerNames[idx] : '';
  };
  readonly boundGetStackEntryTargetName = () => null;

  private resizeObserver: ResizeObserver | null = null;

  /* Phone layout is the game screen's, shared wholesale: game-phone.css is in this
     component's styleUrls and SidePanelComponent brings its own phone rules, so the
     board/hand/panel all reflow exactly like a real match. Only the walkthrough
     chrome — the tooltip and the copy that says "hover" — needs its own handling. */
  readonly isPhoneLayout = signal(false);
  private phoneMedia: MediaQueryList | null = null;
  private readonly onPhoneMediaChange = (e: MediaQueryListEvent) => {
    this.isPhoneLayout.set(e.matches);
    this.updateSpotlight();
  };

  /* The board is sized by the real game's fit model, so a tutorial board renders its
     cards at exactly the size the same board would have in a match — the walkthrough
     tells the player "cards shrink as more permanents enter play", and this is what
     makes that true here. The area's allocated size is measured the same way too. */
  private battlefieldAreaObserver: ResizeObserver | null = null;
  readonly battlefieldAreaSize = signal<{ width: number; height: number }>({ width: 0, height: 0 });
  private readonly fitModel = new BattlefieldFitModel();

  @ViewChild('battlefieldArea')
  set battlefieldArea(ref: ElementRef<HTMLElement> | undefined) {
    this.battlefieldAreaObserver?.disconnect();
    if (!ref) return;
    this.battlefieldAreaObserver ??= new ResizeObserver(entries => {
      const rect = entries[entries.length - 1].contentRect;
      this.battlefieldAreaSize.set({ width: rect.width, height: rect.height });
    });
    this.battlefieldAreaObserver.observe(ref.nativeElement);
  }

  constructor(
    private router: Router,
    private elementRef: ElementRef
  ) {}

  ngAfterViewInit(): void {
    setTimeout(() => this.updateSpotlight(), 100);

    this.resizeObserver = new ResizeObserver(() => this.updateSpotlight());
    this.resizeObserver.observe(this.elementRef.nativeElement);

    this.phoneMedia = window.matchMedia(PHONE_LAYOUT_MEDIA);
    this.isPhoneLayout.set(this.phoneMedia.matches);
    this.phoneMedia.addEventListener('change', this.onPhoneMediaChange);
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    this.battlefieldAreaObserver?.disconnect();
    this.phoneMedia?.removeEventListener('change', this.onPhoneMediaChange);
  }

  // ========== Step navigation ==========

  currentStep = computed(() => this.steps[this.currentStepIndex()]);

  nextStep(): void {
    const idx = this.currentStepIndex();
    if (idx >= this.steps.length - 1) {
      this.exitTutorial();
      return;
    }
    this.currentStepIndex.set(idx + 1);
    setTimeout(() => this.updateSpotlight(), 50);
  }

  prevStep(): void {
    const idx = this.currentStepIndex();
    if (idx > 0) {
      this.currentStepIndex.set(idx - 1);
      setTimeout(() => this.updateSpotlight(), 50);
    }
  }

  exitTutorial(): void {
    this.router.navigate(['/home']);
  }

  // ========== Spotlight ==========

  /**
   * Computes the target element's position relative to `.game-container`
   * (not the viewport). These coordinates are stable regardless of scroll.
   */
  updateSpotlight(): void {
    const step = this.currentStep();
    if (!step.targetSelector) {
      this.spotlightRect.set(null);
      this.scrollToTooltip();
      return;
    }

    const el = this.elementRef.nativeElement.querySelector(step.targetSelector) as HTMLElement | null;
    const container = this.elementRef.nativeElement.querySelector('.game-container') as HTMLElement | null;
    if (!el || !container) {
      this.spotlightRect.set(null);
      return;
    }

    const containerRect = container.getBoundingClientRect();
    const elRect = el.getBoundingClientRect();

    this.spotlightRect.set({
      top: elRect.top - containerRect.top,
      left: elRect.left - containerRect.left,
      width: elRect.width,
      height: elRect.height,
      right: elRect.right - containerRect.left,
      bottom: elRect.bottom - containerRect.top,
      viewportMidY: elRect.top + elRect.height / 2,
    });

    // Scroll target element into view so the user can see it + the tooltip
    el.scrollIntoView({ behavior: 'smooth', block: 'center' });

    // After scroll settles, also ensure the tooltip itself is visible
    setTimeout(() => this.scrollToTooltip(), 350);
  }

  /** Scrolls the tooltip into the visible area if needed. */
  private scrollToTooltip(): void {
    const tooltip = this.elementRef.nativeElement.querySelector('.tutorial-tooltip') as HTMLElement | null;
    if (tooltip && !this.isCenter()) {
      tooltip.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  private isCenter(): boolean {
    return this.currentStep().tooltipPosition === 'center';
  }

  /**
   * On a phone there is no room to park a 400px card next to anything, so the tooltip
   * becomes a sheet pinned to whichever screen edge is AWAY from the thing being
   * explained — otherwise the sheet covers its own subject. Centered steps stay centered.
   */
  phoneTooltipEdge = computed<'top' | 'bottom' | null>(() => {
    if (!this.isPhoneLayout()) return null;
    const rect = this.spotlightRect();
    if (this.currentStep().tooltipPosition === 'center' || !rect) return null;
    return rect.viewportMidY < window.innerHeight / 2 ? 'bottom' : 'top';
  });

  tooltipStyle = computed(() => {
    const step = this.currentStep();
    const rect = this.spotlightRect();

    // Anchored positions assume a desktop-sized viewport; the phone sheet is placed
    // entirely by CSS, and inline styles would outrank it.
    if (this.isPhoneLayout()) {
      return {};
    }

    // Center tooltips use position: fixed via CSS class — no inline style needed
    if (step.tooltipPosition === 'center' || !rect) {
      return {};
    }

    const gap = 16;
    const style: Record<string, string> = {};

    switch (step.tooltipPosition) {
      case 'top':
        style['top'] = `${rect.top - gap}px`;
        style['left'] = `${rect.left + rect.width / 2}px`;
        style['transform'] = 'translateX(-50%) translateY(-100%)';
        break;
      case 'bottom':
        style['top'] = `${rect.bottom + gap}px`;
        style['left'] = `${rect.left + rect.width / 2}px`;
        style['transform'] = 'translateX(-50%)';
        break;
      case 'left':
        style['top'] = `${rect.top + rect.height / 2}px`;
        style['left'] = `${rect.left - gap}px`;
        style['transform'] = 'translateX(-100%) translateY(-50%)';
        break;
      case 'right':
        style['top'] = `${rect.top}px`;
        style['left'] = `${rect.right + gap + 80}px`;
        break;
    }

    return style;
  });

  // ========== Interactive step: tap a land ==========

  onTutorialCardClick(index: number): void {
    const step = this.currentStep();
    if (!step.interactive || step.id !== 'tap-land') return;

    const g = this.game();
    const perm = g.battlefields[0][index];
    if (!perm || perm.tapped || perm.card.type !== 'LAND') return;

    // Mutate mock state: tap the land and add mana
    const updated = structuredClone(g);
    updated.battlefields[0][index].tapped = true;

    // Add green mana for Forest, white for Plains
    const isForest = perm.card.subtypes?.includes('FOREST');
    const manaColor = isForest ? 'G' : 'W';
    updated.manaPool = { ...updated.manaPool };
    updated.manaPool[manaColor] = (updated.manaPool[manaColor] ?? 0) + 1;

    this.game.set(updated);

    // Auto-advance to next step
    setTimeout(() => this.nextStep(), 400);
  }

  isTutorialInteractiveTarget(index: number): boolean {
    const step = this.currentStep();
    if (!step.interactive || step.id !== 'tap-land') return false;
    const perm = this.game().battlefields[0][index];
    return perm != null && !perm.tapped && perm.card.type === 'LAND' && perm.card.subtypes?.includes('FOREST');
  }

  // ========== Battlefield display ==========

  get myBattlefield(): Permanent[] {
    return this.game().battlefields[0] ?? [];
  }

  get opponentBattlefield(): Permanent[] {
    return this.game().battlefields[1] ?? [];
  }

  /** Drives the same "whose turn it is" glow the real board paints on the active half. */
  isActivePlayer(playerIndex: number): boolean {
    const g = this.game();
    return g.activePlayerId === g.playerIds[playerIndex];
  }

  get myLandStacks(): (IndexedPermanent | LandStack)[] {
    return stackBasicLands(splitBattlefield(this.myBattlefield).lands);
  }

  get opponentLandStacks(): (IndexedPermanent | LandStack)[] {
    return stackBasicLands(splitBattlefield(this.opponentBattlefield).lands);
  }

  get myCreatures(): IndexedPermanent[] {
    return splitBattlefield(this.myBattlefield).creatures;
  }

  get opponentCreatures(): IndexedPermanent[] {
    return splitBattlefield(this.opponentBattlefield).creatures;
  }

  // ========== Card sizing (shared with the real game) ==========

  /* The mock board has no auras, no cards exiled with a permanent and no revealed
     rows, so a side is just its two rows. */
  private get mySide(): BattlefieldSide {
    return {
      creatures: this.myCreatures,
      lands: this.myLandStacks,
      isEmpty: this.myBattlefield.length === 0,
      revealedRows: 0,
    };
  }

  private get opponentSide(): BattlefieldSide {
    return {
      creatures: this.opponentCreatures,
      lands: this.opponentLandStacks,
      isEmpty: this.opponentBattlefield.length === 0,
      revealedRows: 0,
    };
  }

  get myBattlefieldZoom(): number {
    return this.fitModel.sideZoom(this.mySide, this.battlefieldAreaSize());
  }

  get opponentBattlefieldZoom(): number {
    return this.fitModel.sideZoom(this.opponentSide, this.battlefieldAreaSize());
  }

  get battlefieldZoom(): number {
    return this.fitModel.boardZoom([this.mySide, this.opponentSide], this.battlefieldAreaSize());
  }

  get handZoom(): number {
    return BattlefieldFitModel.handZoom(this.game().hand.length);
  }

  /** Simple playability check for tutorial: card is playable if total mana >= number of mana symbols in cost. */
  isTutorialCardPlayable(index: number): boolean {
    const card = this.game().hand[index];
    if (!card?.manaCost) return false;
    const pool = this.game().manaPool ?? {};
    const symbols = card.manaCost.match(/\{[^}]+\}/g) ?? [];
    let totalCost = 0;
    for (const sym of symbols) {
      const inner = sym.slice(1, -1);
      const num = parseInt(inner, 10);
      totalCost += isNaN(num) ? 1 : num;
    }
    const totalMana = Object.values(pool).reduce((sum, v) => sum + v, 0);
    return totalMana >= totalCost;
  }

  isLandStack(item: IndexedPermanent | LandStack): item is LandStack {
    return isLandStack(item);
  }

  stackCtx(ip: IndexedPermanent, mine: boolean): TutorialStackContext {
    return { $implicit: ip, mine };
  }

  landStackTrackKey(item: IndexedPermanent | LandStack): string {
    return isLandStack(item) ? `stack:${item.key}` : `land:${item.perm.id}`;
  }

  get manaEntries(): { color: string; count: number }[] {
    return Object.entries(this.game().manaPool ?? {})
      .filter(([, count]) => count > 0)
      .map(([color, count]) => ({ color, count }));
  }

  // ========== Hover ==========

  onCardHover(card: Card, permanent: Permanent | null = null): void {
    this.hoveredCard.set(card);
    this.hoveredPermanent.set(permanent);
  }

  onCardHoverEnd(): void {
    this.hoveredCard.set(null);
    this.hoveredPermanent.set(null);
  }
}
