import { Component, AfterViewInit, OnDestroy, signal, computed, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Game, Card, Permanent, StackEntry, TurnStep, PHASE_GROUPS } from '../../services/websocket.service';
import { CardDisplayComponent } from '../game/card-display/card-display.component';
import { SidePanelComponent } from '../game/side-panel/side-panel.component';
import { IndexedPermanent, LandStack, splitBattlefield, stackBasicLands, isLandStack, isPermanentCreature } from '../game/battlefield.utils';
import { TUTORIAL_MOCK_GAME, TUTORIAL_TAP_FOREST_INDEX } from './tutorial-mock-data';
import { TUTORIAL_STEPS, TutorialStep } from './tutorial-steps';

/** Container-relative rectangle (not viewport-relative). */
interface AbsoluteRect {
  top: number;
  left: number;
  width: number;
  height: number;
  right: number;
  bottom: number;
}

@Component({
  selector: 'app-tutorial',
  standalone: true,
  imports: [CommonModule, CardDisplayComponent, SidePanelComponent],
  templateUrl: './tutorial.component.html',
  styleUrls: ['../game/shared-game-styles.css', './tutorial.component.css']
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

  constructor(
    private router: Router,
    private elementRef: ElementRef
  ) {}

  ngAfterViewInit(): void {
    setTimeout(() => this.updateSpotlight(), 100);

    this.resizeObserver = new ResizeObserver(() => this.updateSpotlight());
    this.resizeObserver.observe(this.elementRef.nativeElement);
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
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

  tooltipStyle = computed(() => {
    const step = this.currentStep();
    const rect = this.spotlightRect();

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

  landStackTrackKey(item: IndexedPermanent | LandStack): string {
    return isLandStack(item) ? item.lands[0].perm.id : item.perm.id;
  }

  get manaEntries(): { color: string; count: number }[] {
    return Object.entries(this.game().manaPool ?? {})
      .filter(([, count]) => count > 0)
      .map(([color, count]) => ({ color, count }));
  }

  get totalMana(): number {
    return Object.values(this.game().manaPool ?? {}).reduce((sum, v) => sum + v, 0);
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
