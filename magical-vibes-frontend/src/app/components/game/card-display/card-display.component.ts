import { Component, Input, HostBinding, OnInit, OnChanges, AfterViewChecked, SimpleChanges, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Card, Permanent } from '../../../services/websocket.service';
import { ScryfallImageService } from '../../../services/scryfall-image.service';
import { ScryfallCardDataService } from '../../../services/scryfall-card-data.service';
import { ManaSymbolService } from '../../../services/mana-symbol.service';
import { SetSymbolService } from '../../../services/set-symbol.service';
import { WatermarkService } from '../../../services/watermark.service';
import { formatEnumName, formatKeywords, formatTypeLine } from '../../../utils/format-utils';

@Component({
  selector: 'app-card-display',
  standalone: true,
  templateUrl: './card-display.component.html',
  styleUrl: './card-display.component.css',
  host: { 'class': 'card' }
})
export class CardDisplayComponent implements OnInit, OnChanges, AfterViewChecked {
  @Input({ required: true }) card!: Card;
  @Input() permanent: Permanent | null = null;
  @Input() preview = false;

  formatKeywords = formatKeywords;
  formatEnumName = formatEnumName;
  artUrl = signal<string | null>(null);
  watermarkUrl = signal<string | null>(null);

  @ViewChild('textBox') textBoxRef?: ElementRef<HTMLDivElement>;

  private static readonly MAX_FONT_SIZE = 11;
  private static readonly MIN_FONT_SIZE = 7;
  private static readonly FONT_STEP = 0.5;
  private static readonly FLAVOR_REDUCTION = 2;
  private lastTextFingerprint = '';

  private scryfallImageService = inject(ScryfallImageService);
  private scryfallCardDataService = inject(ScryfallCardDataService);
  private manaSymbolService = inject(ManaSymbolService);
  private setSymbolService = inject(SetSymbolService);
  private watermarkService = inject(WatermarkService);
  private sanitizer = inject(DomSanitizer);

  ngOnInit(): void {
    this.fetchCardArt();
    this.fetchWatermark();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['card'] && !changes['card'].firstChange) {
      if (this.card.setCode && this.card.collectorNumber) {
        const cached = this.scryfallImageService.getCachedArtCropUrl(this.card.setCode, this.card.collectorNumber);
        if (cached) {
          this.artUrl.set(cached);
        } else {
          this.artUrl.set(null);
          this.fetchCardArt();
        }
      } else {
        this.artUrl.set(null);
      }

      if (this.card.watermark) {
        const cachedWm = this.watermarkService.getCachedWatermarkUrl(this.card.watermark);
        if (cachedWm) {
          this.watermarkUrl.set(cachedWm);
        } else {
          this.watermarkUrl.set(null);
          this.fetchWatermark();
        }
      } else {
        this.watermarkUrl.set(null);
      }
    }
  }

  private fetchWatermark(): void {
    if (this.card.watermark) {
      this.watermarkService.getWatermarkUrl(this.card.watermark)
        .then(url => this.watermarkUrl.set(url))
        .catch(() => { this.watermarkUrl.set(null); });
    }
  }

  private fetchCardArt(): void {
    if (this.card.setCode && this.card.collectorNumber) {
      this.scryfallImageService.getArtCropUrl(this.card.setCode, this.card.collectorNumber)
        .then(url => this.artUrl.set(url))
        .catch(() => { this.artUrl.set(null); });
    }
  }

  private static readonly COLOR_CSS_MAP: Record<string, string> = {
    'BLACK': '#1a1a20',
    'GREEN': '#4a7c28',
    'BLUE': '#2c5ea2',
    'RED': '#a03030',
    'WHITE': '#f0e6b2',
  };

  @HostBinding('class.token-card')
  get isToken(): boolean {
    return this.card.token;
  }

  @HostBinding('attr.data-card-color')
  get cardColor(): string | null {
    const colors = this.card.colors;
    if (colors && colors.length > 1) {
      return 'MULTICOLOR';
    }
    return colors && colors.length === 1 ? colors[0] : this.card.color;
  }

  @HostBinding('style.background')
  get multicolorBackground(): string | null {
    const colors = this.card.colors;
    if (!colors || colors.length <= 1) {
      return null;
    }
    const cssColors = colors
      .map(c => CardDisplayComponent.COLOR_CSS_MAP[c])
      .filter((c): c is string => c != null);
    if (cssColors.length < 2) {
      return null;
    }
    if (cssColors.length === 2) {
      return `linear-gradient(135deg, ${cssColors[0]} 0%, ${cssColors[1]} 100%)`;
    }
    const stops = cssColors.map((c, i) =>
      `${c} ${Math.round((i / (cssColors.length - 1)) * 100)}%`
    );
    return `linear-gradient(135deg, ${stops.join(', ')})`;
  }

  @HostBinding('class.is-tapped')
  get isTapped(): boolean {
    return !this.preview && !!this.permanent?.tapped;
  }

  get effectiveKeywords(): string[] {
    if (this.permanent && this.permanent.grantedKeywords) {
      return this.permanent.grantedKeywords.filter(kw => !this.card.keywords.includes(kw));
    }
    return [];
  }

  get isBuffed(): boolean {
    return this.permanent != null &&
      (this.permanent.powerModifier > 0 || this.permanent.toughnessModifier > 0);
  }

  get isDebuffed(): boolean {
    return this.permanent != null &&
      (this.permanent.powerModifier < 0 || this.permanent.toughnessModifier < 0);
  }

  get isDamaged(): boolean {
    return this.permanent != null && this.permanent.markedDamage > 0;
  }

  get displayPower(): number | null {
    if (this.permanent?.animatedCreature) return this.permanent.effectivePower;
    if (this.card.power == null) return null;
    return this.permanent ? this.permanent.effectivePower : this.card.power;
  }

  get displayToughness(): number | null {
    if (this.permanent?.animatedCreature) return this.permanent.effectiveToughness;
    if (this.card.toughness == null) return null;
    return this.permanent ? this.permanent.effectiveToughness : this.card.toughness;
  }

  /** Number of counters of the given type on this permanent (0 if none or no permanent). */
  counter(counterType: string): number {
    return this.permanent?.counters?.[counterType] ?? 0;
  }

  /** Counter types shown elsewhere on the card: loyalty has its own box, P/T counters are baked into effective P/T. */
  private static readonly BADGE_EXCLUDED_COUNTERS = new Set(['LOYALTY', 'PLUS_ONE_PLUS_ONE', 'MINUS_ONE_MINUS_ONE']);

  get badgeCounters(): { type: string; count: number }[] {
    if (this.preview || !this.permanent?.counters) return [];
    return Object.entries(this.permanent.counters)
      .filter(([type, count]) => count > 0 && !CardDisplayComponent.BADGE_EXCLUDED_COUNTERS.has(type))
      .map(([type, count]) => ({ type, count }));
  }

  get displayLoyalty(): number | null {
    if (this.permanent && this.counter('LOYALTY') > 0) return this.counter('LOYALTY');
    return this.card.loyalty ?? null;
  }

  get typeLine(): string {
    return formatTypeLine(this.card);
  }

  get scryfallData() {
    if (!this.card.setCode || !this.card.collectorNumber) return null;
    return this.scryfallCardDataService.getCardData(this.card.setCode, this.card.collectorNumber);
  }

  get flavorText(): string | null {
    return this.scryfallData?.flavorText ?? null;
  }

  get artist(): string | null {
    return this.scryfallData?.artist ?? null;
  }

  get rarity(): string | null {
    return this.scryfallData?.rarity ?? null;
  }

  ngAfterViewChecked(): void {
    this.fitTextToBox();
  }

  private fitTextToBox(): void {
    const el = this.textBoxRef?.nativeElement;
    if (!el) return;

    const fp = (this.card.cardText ?? '') + '|' +
      this.formatKeywords(this.effectiveKeywords) + '|' +
      (this.flavorText ?? '');
    if (fp === this.lastTextFingerprint) return;
    this.lastTextFingerprint = fp;

    const flavorEl = el.querySelector('.card-flavor-text') as HTMLElement | null;
    let size = CardDisplayComponent.MAX_FONT_SIZE;

    while (size >= CardDisplayComponent.MIN_FONT_SIZE) {
      el.style.fontSize = size + 'px';
      if (flavorEl) flavorEl.style.fontSize = (size - CardDisplayComponent.FLAVOR_REDUCTION) + 'px';

      if (el.scrollHeight <= el.clientHeight) break;
      size -= CardDisplayComponent.FONT_STEP;
    }
  }

  get setSymbolUrl(): string | null {
    if (!this.card.setCode) return null;
    return this.setSymbolService.getSymbolUrl(this.card.setCode);
  }

  get formattedManaCost(): SafeHtml {
    if (!this.card.manaCost) return '';
    return this.sanitizer.bypassSecurityTrustHtml(
      this.manaSymbolService.replaceSymbols(this.card.manaCost)
    );
  }

  get formattedCardText(): SafeHtml {
    if (!this.card.cardText) return '';
    return this.sanitizer.bypassSecurityTrustHtml(
      this.manaSymbolService.replaceSymbols(this.card.cardText)
    );
  }

}
