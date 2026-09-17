import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, MessageType, BrowseCardInfo, CardListResponse, SetInfo, Card, SaveDeckResponse, SavedDeck, SavedDeckEntry, DeckFormat, DeckValidation, DECK_FORMATS } from '../../services/websocket.service';
import { CardDisplayComponent } from '../game/card-display/card-display.component';
import { browseInfoToCard } from '../../utils/browse-card-utils';
import { manaSymbolHtml } from '../../utils/mana-symbols';
import { Subscription } from 'rxjs';

interface DeckEntry {
  cardInfo: BrowseCardInfo;
  count: number;
}

interface CardSection {
  id: string;
  label: string | null;
  cards: BrowseCardInfo[];
}

@Component({
  selector: 'app-deck-builder',
  standalone: true,
  imports: [CommonModule, FormsModule, CardDisplayComponent],
  templateUrl: './deck-builder.component.html',
  styleUrl: './deck-builder.component.css'
})
export class DeckBuilderComponent implements OnInit, OnDestroy {
  readonly formats = DECK_FORMATS;
  format = signal<DeckFormat>('CASUAL');
  sideboard = signal<DeckEntry[]>([]);
  commander = signal<BrowseCardInfo | null>(null);
  addTo = signal<'main' | 'sideboard'>('main');
  validation = signal<DeckValidation | null>(null);
  error = signal('');
  editingId = signal<string | undefined>(undefined);
  sideboardCount = computed(() => this.sideboard().reduce((sum, entry) => sum + entry.count, 0));
  private pendingDeck: SavedDeck | null = null;
  private pendingSets: string[] = [];
  private pendingSet: string | null = null;
  private loadedCards = new Map<string, BrowseCardInfo>();
  get savedDecks() { return this.websocketService.availableDecks.filter(deck => deck.id.startsWith('custom-')); }

  cards = signal<BrowseCardInfo[]>([]);
  selectedSetCode = signal('');
  searchQuery = signal('');
  statusFilter = signal<'all' | 'implemented' | 'not-implemented'>('implemented');
  viewMode = signal<'list' | 'card'>('list');
  loading = signal(false);
  sortColumn = signal<'number' | 'name' | 'implemented' | null>('number');
  sortDirection = signal<'asc' | 'desc'>('asc');

  deckEntries = signal<DeckEntry[]>([]);
  showSavePopup = signal(false);
  deckName = signal('');
  saving = signal(false);
  /** set:collectorNumber keys of double-faced cards currently showing their back face */
  flippedCards = signal<Set<string>>(new Set());

  private subscriptions: Subscription[] = [];

  filteredCards = computed(() => {
    let result = this.cards().filter(card => !/^(Plane|Phenomenon)(?: |$)/.test(card.typeLine));
    const query = this.searchQuery().toLowerCase().trim();
    if (query) {
      result = result.filter(c => c.name.toLowerCase().includes(query));
    }
    const status = this.statusFilter();
    if (status === 'implemented') {
      result = result.filter(c => c.implemented);
    } else if (status === 'not-implemented') {
      result = result.filter(c => !c.implemented);
    }

    const col = this.sortColumn();
    if (col) {
      const dir = this.sortDirection() === 'asc' ? 1 : -1;
      result = [...result].sort((a, b) => {
        if (col === 'number') {
          return (parseInt(a.collectorNumber, 10) - parseInt(b.collectorNumber, 10)) * dir;
        } else if (col === 'name') {
          return a.name.localeCompare(b.name) * dir;
        } else {
          return ((a.implemented === b.implemented) ? 0 : a.implemented ? -1 : 1) * dir;
        }
      });
    }

    return result;
  });

  /**
   * Splits the filtered list so numbered set extras (planeswalker-deck / set-extension) sit under
   * their own headers when present. Ordinary sets keep a flat list (single unlabeled section).
   */
  cardSections = computed((): CardSection[] => {
    const cards = this.filteredCards();
    const main: BrowseCardInfo[] = [];
    const planeswalkerDeck: BrowseCardInfo[] = [];
    const setExtension: BrowseCardInfo[] = [];
    for (const card of cards) {
      const types = card.promoTypes ?? [];
      if (types.includes('planeswalkerdeck')) {
        planeswalkerDeck.push(card);
      } else if (types.includes('setextension')) {
        setExtension.push(card);
      } else {
        main.push(card);
      }
    }
    if (planeswalkerDeck.length === 0 && setExtension.length === 0) {
      return [{ id: 'main', label: null, cards: main }];
    }
    const sections: CardSection[] = [];
    if (main.length > 0) {
      sections.push({ id: 'main', label: 'Main Set', cards: main });
    }
    if (planeswalkerDeck.length > 0) {
      sections.push({ id: 'planeswalkerdeck', label: 'Planeswalker Deck', cards: planeswalkerDeck });
    }
    if (setExtension.length > 0) {
      sections.push({ id: 'setextension', label: 'Set Extension', cards: setExtension });
    }
    return sections;
  });

  totalCount = computed(() => this.cards().length);
  implementedCount = computed(() => this.cards().filter(c => c.implemented).length);
  filteredCount = computed(() => this.filteredCards().length);
  deckCardCount = computed(() => this.deckEntries().reduce((sum, e) => sum + e.count, 0));

  constructor(
    private router: Router,
    public websocketService: WebsocketService
  ) {}

  get availableSets(): SetInfo[] {
    return this.websocketService.availableSets;
  }

  ngOnInit() {
    if (!this.websocketService.isConnected()) {
      this.router.navigate(['/']);
      return;
    }

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        if (message.type === MessageType.CARD_LIST_RESPONSE) {
          const response = message as CardListResponse;
          for (const card of response.cards) this.loadedCards.set(`${card.setCode.toUpperCase()}:${card.collectorNumber}`, card);
          if (this.pendingDeck) {
            if (response.setCode.toUpperCase() === this.pendingSet?.toUpperCase()) this.loadNextDeckSet();
          } else {
            this.cards.set(response.cards);
            this.loading.set(false);
          }
        } else if (message.type === MessageType.SAVE_DECK_RESPONSE) {
          const response = message as SaveDeckResponse;
          this.websocketService.availableDecks = [response.deck, ...this.websocketService.availableDecks.filter(deck => deck.id !== response.deck.id)];
          this.editingId.set(response.deck.id);
          this.validation.set(response.deck.validation ?? null);
          this.saving.set(false);
          this.showSavePopup.set(false);
        } else if (message.type === MessageType.VALIDATE_DECK_RESPONSE) {
          this.validation.set((message as unknown as { validation: DeckValidation }).validation);
        } else if (message.type === MessageType.LOAD_DECK_RESPONSE) {
          this.pendingDeck = (message as unknown as { deck: SavedDeck }).deck;
          const deck = this.pendingDeck;
          this.pendingSets = [...new Set([...deck.entries, ...deck.sideboard, ...(deck.commander ? [deck.commander] : [])].map(entry => entry.setCode))];
          this.loadNextDeckSet();
        } else if (message.type === MessageType.ERROR) {
          this.saving.set(false);
          this.loading.set(false);
          this.error.set((message as unknown as { message: string }).message);

        }
      })
    );

    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        this.router.navigate(['/']);
      })
    );

    if (this.availableSets.length > 0) {
      this.selectedSetCode.set(this.availableSets[0].code);
      this.requestCardList();
    }
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  onSetChange(setCode: string) {
    this.selectedSetCode.set(setCode);
    this.searchQuery.set('');
    this.requestCardList();
  }

  requestCardList() {
    this.loading.set(true);
    this.cards.set([]);
    this.websocketService.send({
      type: MessageType.REQUEST_CARD_LIST,
      setCode: this.selectedSetCode()
    });
  }

  toggleSort(column: 'number' | 'name' | 'implemented') {
    if (this.sortColumn() === column) {
      if (this.sortDirection() === 'asc') {
        this.sortDirection.set('desc');
      } else {
        this.sortColumn.set(null);
        this.sortDirection.set('asc');
      }
    } else {
      this.sortColumn.set(column);
      this.sortDirection.set('asc');
    }
  }

  sortIndicator(column: 'number' | 'name' | 'implemented'): string {
    if (this.sortColumn() !== column) return '';
    return this.sortDirection() === 'asc' ? ' \u25B2' : ' \u25BC';
  }

  addCardToDeck(card: BrowseCardInfo) {
    if (!card.implemented) return;

    this.validation.set(null);
    const destination = this.addTo() === 'sideboard' ? this.sideboard : this.deckEntries;
    const entries = [...destination()];

    const existing = entries.find(e => e.cardInfo.setCode === card.setCode && e.cardInfo.collectorNumber === card.collectorNumber);
    if (existing) {
      existing.count++;
      destination.set([...entries]);
    } else {
      entries.push({ cardInfo: card, count: 1 });
      destination.set(entries);
    }
  }

  removeCardFromDeck(index: number) {
    this.validation.set(null);
    const entries = [...this.deckEntries()];
    if (entries[index].count > 1) {
      entries[index] = { ...entries[index], count: entries[index].count - 1 };
      this.deckEntries.set(entries);
    } else {
      entries.splice(index, 1);
      this.deckEntries.set(entries);
    }
  }

  openSavePopup() {
    this.showSavePopup.set(true);
  }

  closeSavePopup() {
    this.showSavePopup.set(false);
  }

  saveDeck() {
    const name = this.deckName().trim();
    if (!name) return;

    this.saving.set(true);
    this.error.set('');
    this.websocketService.send({ type: MessageType.SAVE_DECK, ...this.definition() });
  }

  private entry(card: BrowseCardInfo, count = 1): SavedDeckEntry {
    return { setCode: card.setCode, collectorNumber: card.collectorNumber, count };
  }
  private definition(): SavedDeck {
    return { id: this.editingId(), name: this.deckName().trim(), format: this.format(),
      entries: this.deckEntries().map(e => this.entry(e.cardInfo, e.count)),
      sideboard: this.sideboard().map(e => this.entry(e.cardInfo, e.count)),
      commander: this.commander() ? this.entry(this.commander()!) : null };
  }
  validateDeck() {
    this.error.set('');
    this.websocketService.send({ type: MessageType.VALIDATE_DECK, deck: this.definition() });
  }
  changeFormat(format: DeckFormat) {
    this.format.set(format);
    this.validation.set(null);
  }
  selectCommander(index: number, event: Event) {
    event.stopPropagation();
    const card = this.deckEntries()[index].cardInfo;
    this.clearCommander();
    this.removeCardFromDeck(index);
    this.commander.set(card);
  }
  clearCommander() {
    const card = this.commander();
    if (card) {
      const existing = this.deckEntries().find(entry => entry.cardInfo.setCode === card.setCode && entry.cardInfo.collectorNumber === card.collectorNumber);
      this.deckEntries.set(existing ? this.deckEntries().map(entry => entry === existing ? { ...entry, count: entry.count + 1 } : entry)
        : [...this.deckEntries(), { cardInfo: card, count: 1 }]);
      this.commander.set(null);
      this.validation.set(null);
    }
  }
  removeSideboard(index: number) {
    this.sideboard.update(entries => entries.flatMap((entry, i) => i !== index ? [entry] : entry.count > 1 ? [{ ...entry, count: entry.count - 1 }] : []));
    this.validation.set(null);
  }
  newDeck() {
    this.editingId.set(undefined); this.deckName.set(''); this.deckEntries.set([]);
    this.sideboard.set([]); this.commander.set(null); this.validation.set(null); this.error.set('');
  }
  loadDeck(id: string) {
    if (!id) return;
    this.error.set('');
    this.loading.set(true);
    this.websocketService.send({ type: MessageType.LOAD_DECK, id });
  }
  private loadNextDeckSet() {
    if (this.pendingSets.length) {
      this.pendingSet = this.pendingSets.shift()!;
      this.websocketService.send({ type: MessageType.REQUEST_CARD_LIST, setCode: this.pendingSet });
      return;
    }
    const deck = this.pendingDeck!;
    const lookup = (entry: SavedDeckEntry): DeckEntry => {
      const cardInfo = this.loadedCards.get(`${entry.setCode.toUpperCase()}:${entry.collectorNumber}`);
      if (!cardInfo) throw new Error(`Card ${entry.setCode} ${entry.collectorNumber} is unavailable`);
      return { cardInfo, count: entry.count };
    };
    try {
      const main = deck.entries.map(lookup);
      const sideboard = deck.sideboard.map(lookup);
      const commander = deck.commander ? lookup(deck.commander).cardInfo : null;
      this.deckEntries.set(main); this.sideboard.set(sideboard); this.commander.set(commander);
      this.format.set(deck.format); this.deckName.set(deck.name); this.editingId.set(deck.id);
      this.validation.set(null);
    } catch (error) { this.error.set(String(error)); }
    this.pendingDeck = null;
    this.pendingSet = null;
    this.loading.set(false);
    this.requestCardList();
  }

  goBack() {
    this.router.navigate(['/home']);
  }

  toCard(info: BrowseCardInfo): Card {
    return browseInfoToCard(info);
  }

  private flipKey(info: BrowseCardInfo): string {
    return `${info.setCode}:${info.collectorNumber}`;
  }

  isFlipped(info: BrowseCardInfo): boolean {
    return info.backFace != null && this.flippedCards().has(this.flipKey(info));
  }

  toggleFlip(info: BrowseCardInfo, event: Event): void {
    event.stopPropagation();
    const flipped = new Set(this.flippedCards());
    const key = this.flipKey(info);
    if (flipped.has(key)) {
      flipped.delete(key);
    } else {
      flipped.add(key);
    }
    this.flippedCards.set(flipped);
  }

  /** The face to render: the back face when this double-faced card is flipped. */
  displayInfo(info: BrowseCardInfo): BrowseCardInfo {
    return this.isFlipped(info) ? info.backFace! : info;
  }

  renderManaCost(manaCost: string | null): string {
    if (!manaCost) return '';
    return manaSymbolHtml(manaCost);
  }
}
