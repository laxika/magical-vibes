import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, MessageType, BrowseCardInfo, CardListResponse, SetInfo, Card } from '../../services/websocket.service';
import { ManaSymbolService } from '../../services/mana-symbol.service';
import { CardDisplayComponent } from '../game/card-display/card-display.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-card-browser',
  standalone: true,
  imports: [CommonModule, FormsModule, CardDisplayComponent],
  templateUrl: './card-browser.component.html',
  styleUrl: './card-browser.component.css'
})
export class CardBrowserComponent implements OnInit, OnDestroy {
  cards = signal<BrowseCardInfo[]>([]);
  selectedSetCode = signal('');
  searchQuery = signal('');
  statusFilter = signal<'all' | 'implemented' | 'not-implemented'>('all');
  viewMode = signal<'list' | 'card'>('list');
  loading = signal(false);
  sortColumn = signal<'number' | 'name' | 'implemented' | null>('number');
  sortDirection = signal<'asc' | 'desc'>('asc');
  private subscriptions: Subscription[] = [];

  filteredCards = computed(() => {
    let result = this.cards();
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

  totalCount = computed(() => this.cards().length);
  implementedCount = computed(() => this.cards().filter(c => c.implemented).length);
  implementedPercent = computed(() => {
    const total = this.totalCount();
    if (total === 0) return '0';
    return ((this.implementedCount() / total) * 100).toFixed(1);
  });
  filteredCount = computed(() => this.filteredCards().length);

  constructor(
    private router: Router,
    public websocketService: WebsocketService,
    public manaSymbolService: ManaSymbolService
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
          this.cards.set(response.cards);
          this.loading.set(false);
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

  goBack() {
    this.router.navigate(['/home']);
  }

  toCard(info: BrowseCardInfo): Card {
    const powerNum = info.power != null ? (parseInt(info.power, 10) || 0) : null;
    const toughnessNum = info.toughness != null ? (parseInt(info.toughness, 10) || 0) : null;

    return {
      id: null,
      name: info.name,
      type: info.type,
      additionalTypes: info.additionalTypes ?? [],
      supertypes: info.supertypes ?? [],
      subtypes: info.subtypes ?? [],
      cardText: info.cardText,
      manaCost: info.manaCost,
      power: powerNum,
      toughness: toughnessNum,
      keywords: info.keywords ?? [],
      hasTapAbility: false,
      setCode: info.setCode,
      collectorNumber: info.collectorNumber,
      color: info.color,
      needsTarget: false,
      needsSpellTarget: false,
      activatedAbilities: [],
      loyalty: info.loyalty,
      hasConvoke: false,
      colors: info.color ? [info.color] : [],
      hasPhyrexianMana: (info.manaCost ?? '').includes('/P'),
      phyrexianManaCount: ((info.manaCost ?? '').match(/\/P/g) || []).length,
      token: false,
      watermark: null,
      hasAlternateCastingCost: false,
      alternateCostLifePayment: 0,
      alternateCostSacrificeCount: 0,
      graveyardActivatedAbilities: [],
      transformable: false,
      kickerCost: null
    };
  }

  renderManaCost(manaCost: string | null): string {
    if (!manaCost) return '';
    return this.manaSymbolService.replaceSymbols(manaCost);
  }

  capitalizeRarity(rarity: string): string {
    return rarity.charAt(0).toUpperCase() + rarity.slice(1);
  }
}
