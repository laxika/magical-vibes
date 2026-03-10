import { Injectable } from '@angular/core';
import {
  WebsocketService, MessageType, Card,
  ScryNotification,
  ReorderLibraryCardsNotification,
  ChooseCardFromLibraryNotification,
  ChooseHandTopBottomNotification
} from './websocket.service';

@Injectable({ providedIn: 'root' })
export class LibraryChoiceService {

  constructor(private websocketService: WebsocketService) {}

  // --- Scry state ---
  scrying = false;
  scryCards: Card[] = [];
  scryPrompt = '';
  scryTopIndices: number[] = [];
  scryBottomIndices: number[] = [];

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

  reset(): void {
    this.scrying = false;
    this.scryCards = [];
    this.scryPrompt = '';
    this.scryTopIndices = [];
    this.scryBottomIndices = [];
    this.searchingLibrary = false;
    this.librarySearchCards = [];
    this.librarySearchPrompt = '';
    this.librarySearchCanFailToFind = false;
    this.reorderingLibrary = false;
    this.reorderAllCards = [];
    this.reorderAvailableIndices = [];
    this.reorderOriginalIndices = [];
    this.reorderPrompt = '';
    this.choosingHandTopBottom = false;
    this.handTopBottomCards = [];
    this.handTopBottomHandIndex = null;
    this.handTopBottomTopIndex = null;
  }

  // ========== Message handlers ==========

  handleScry(msg: ScryNotification): void {
    this.scrying = true;
    this.scryCards = msg.cards;
    this.scryPrompt = msg.prompt;
    this.scryTopIndices = [];
    this.scryBottomIndices = [];
  }

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

  // ========== Scry ==========

  get scryAvailableCards(): { card: Card; originalIndex: number }[] {
    return this.scryCards
      .map((card, i) => ({ card, originalIndex: i }))
      .filter(item => !this.scryTopIndices.includes(item.originalIndex) && !this.scryBottomIndices.includes(item.originalIndex));
  }

  get scryTopCards(): { card: Card; originalIndex: number; position: number }[] {
    return this.scryTopIndices.map((origIdx, pos) => ({
      card: this.scryCards[origIdx],
      originalIndex: origIdx,
      position: pos + 1
    }));
  }

  get scryBottomCards(): { card: Card; originalIndex: number }[] {
    return this.scryBottomIndices.map(origIdx => ({
      card: this.scryCards[origIdx],
      originalIndex: origIdx
    }));
  }

  scryToTop(originalIndex: number): void {
    this.scryTopIndices = [...this.scryTopIndices, originalIndex];
  }

  scryToBottom(originalIndex: number): void {
    this.scryBottomIndices = [...this.scryBottomIndices, originalIndex];
  }

  undoScry(): void {
    // Undo last action from either pile
    if (this.scryBottomIndices.length > 0 && (this.scryTopIndices.length === 0 ||
        this.scryBottomIndices.length >= this.scryTopIndices.length)) {
      this.scryBottomIndices = this.scryBottomIndices.slice(0, -1);
    } else if (this.scryTopIndices.length > 0) {
      this.scryTopIndices = this.scryTopIndices.slice(0, -1);
    }
  }

  undoScryTop(): void {
    if (this.scryTopIndices.length > 0) {
      this.scryTopIndices = this.scryTopIndices.slice(0, -1);
    }
  }

  undoScryBottom(): void {
    if (this.scryBottomIndices.length > 0) {
      this.scryBottomIndices = this.scryBottomIndices.slice(0, -1);
    }
  }

  confirmScry(): void {
    this.websocketService.send({
      type: MessageType.SCRY_COMPLETED,
      topCardOrder: this.scryTopIndices,
      bottomCardOrder: this.scryBottomIndices
    });
    this.scrying = false;
    this.scryCards = [];
    this.scryPrompt = '';
    this.scryTopIndices = [];
    this.scryBottomIndices = [];
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
