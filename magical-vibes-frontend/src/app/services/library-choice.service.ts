import { Injectable } from '@angular/core';
import {
  WebsocketService, MessageType, Card,
  ReorderLibraryCardsNotification,
  ChooseCardFromLibraryNotification,
  ChooseHandTopBottomNotification
} from './websocket.service';

@Injectable({ providedIn: 'root' })
export class LibraryChoiceService {

  constructor(private websocketService: WebsocketService) {}

  // --- Library search state ---
  searchingLibrary = false;
  librarySearchCards: Card[] = [];
  librarySearchPrompt = '';
  librarySearchCanFailToFind = false;

  // --- Library reorder state ---
  reorderingLibrary = false;
  reorderAllCards: Card[] = [];
  reorderAvailableIndices: number[] = [];
  reorderOriginalIndices: number[] = [];
  reorderPrompt = '';

  // --- Telling Time state ---
  choosingHandTopBottom = false;
  handTopBottomCards: Card[] = [];
  handTopBottomHandIndex: number | null = null;
  handTopBottomTopIndex: number | null = null;

  // ========== Message handlers ==========

  handleReorderLibraryCards(msg: ReorderLibraryCardsNotification): void {
    this.reorderingLibrary = true;
    this.reorderAllCards = msg.cards;
    this.reorderAvailableIndices = msg.cards.map((_, i) => i);
    this.reorderOriginalIndices = [];
    this.reorderPrompt = msg.prompt;
  }

  handleChooseCardFromLibrary(msg: ChooseCardFromLibraryNotification): void {
    this.searchingLibrary = true;
    this.librarySearchCards = msg.cards;
    this.librarySearchPrompt = msg.prompt;
    this.librarySearchCanFailToFind = msg.canFailToFind;
  }

  handleChooseHandTopBottom(msg: ChooseHandTopBottomNotification): void {
    this.choosingHandTopBottom = true;
    this.handTopBottomCards = msg.cards;
    this.handTopBottomHandIndex = null;
    this.handTopBottomTopIndex = null;
  }

  // ========== Library search ==========

  chooseLibraryCard(index: number): void {
    if (!this.searchingLibrary) return;
    this.websocketService.send({
      type: MessageType.LIBRARY_CARD_CHOSEN,
      cardIndex: index
    });
    this.searchingLibrary = false;
    this.librarySearchCards = [];
    this.librarySearchPrompt = '';
    this.librarySearchCanFailToFind = false;
  }

  declineLibrarySearch(): void {
    if (!this.searchingLibrary) return;
    this.websocketService.send({
      type: MessageType.LIBRARY_CARD_CHOSEN,
      cardIndex: -1
    });
    this.searchingLibrary = false;
    this.librarySearchCards = [];
    this.librarySearchPrompt = '';
    this.librarySearchCanFailToFind = false;
  }

  // ========== Library reorder ==========

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

  // ========== Hand/Top/Bottom (Telling Time) ==========

  get handTopBottomStep(): number {
    if (this.handTopBottomHandIndex === null) return 0;
    if (this.handTopBottomTopIndex === null) return 1;
    return 2;
  }

  get handTopBottomPrompt(): string {
    if (this.handTopBottomStep === 0) return 'Choose a card to put into your hand:';
    if (this.handTopBottomStep === 1) return 'Choose a card to put on top of your library:';
    return 'Confirm your choices:';
  }

  get handTopBottomAvailableCards(): { card: Card; originalIndex: number }[] {
    return this.handTopBottomCards
      .map((card, i) => ({ card, originalIndex: i }))
      .filter(item => item.originalIndex !== this.handTopBottomHandIndex && item.originalIndex !== this.handTopBottomTopIndex);
  }

  selectHandTopBottomCard(originalIndex: number): void {
    if (this.handTopBottomHandIndex === null) {
      this.handTopBottomHandIndex = originalIndex;
      // If only 2 cards total, auto-select the remaining one for top
      const remaining = this.handTopBottomCards
        .map((_, i) => i)
        .filter(i => i !== this.handTopBottomHandIndex);
      if (remaining.length === 1) {
        this.handTopBottomTopIndex = remaining[0];
      }
    } else if (this.handTopBottomTopIndex === null) {
      this.handTopBottomTopIndex = originalIndex;
    }
  }

  undoHandTopBottom(): void {
    if (this.handTopBottomTopIndex !== null) {
      this.handTopBottomTopIndex = null;
    } else if (this.handTopBottomHandIndex !== null) {
      this.handTopBottomHandIndex = null;
    }
  }

  confirmHandTopBottom(): void {
    if (this.handTopBottomHandIndex === null || this.handTopBottomTopIndex === null) return;
    this.websocketService.send({
      type: MessageType.HAND_TOP_BOTTOM_CHOSEN,
      handCardIndex: this.handTopBottomHandIndex,
      topCardIndex: this.handTopBottomTopIndex
    });
    this.choosingHandTopBottom = false;
    this.handTopBottomCards = [];
    this.handTopBottomHandIndex = null;
    this.handTopBottomTopIndex = null;
  }
}
