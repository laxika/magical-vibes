import { Component, Input, HostBinding, OnInit, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Card, Permanent } from '../../../services/websocket.service';
import { ScryfallImageService } from '../../../services/scryfall-image.service';
import { ScryfallCardDataService } from '../../../services/scryfall-card-data.service';
import { ManaSymbolService } from '../../../services/mana-symbol.service';
import { SetSymbolService } from '../../../services/set-symbol.service';

@Component({
  selector: 'app-card-display',
  standalone: true,
  templateUrl: './card-display.component.html',
  styleUrl: './card-display.component.css',
  host: { 'class': 'card' }
})
export class CardDisplayComponent implements OnInit, OnChanges {
  @Input({ required: true }) card!: Card;
  @Input() permanent: Permanent | null = null;
  @Input() preview = false;

  artUrl = signal<string | null>(null);

  private scryfallImageService = inject(ScryfallImageService);
  private scryfallCardDataService = inject(ScryfallCardDataService);
  private manaSymbolService = inject(ManaSymbolService);
  private setSymbolService = inject(SetSymbolService);
  private sanitizer = inject(DomSanitizer);

  ngOnInit(): void {
    this.fetchCardArt();
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
    }
  }

  private fetchCardArt(): void {
    if (this.card.setCode && this.card.collectorNumber) {
      this.scryfallImageService.getArtCropUrl(this.card.setCode, this.card.collectorNumber)
        .then(url => this.artUrl.set(url))
        .catch(() => { this.artUrl.set(null); });
    }
  }

  @HostBinding('attr.data-card-color')
  get cardColor(): string | null {
    return this.card.color;
  }

  @HostBinding('style.transform')
  get tappedTransform(): string | null {
    return !this.preview && this.permanent?.tapped ? 'rotate(90deg)' : null;
  }

  @HostBinding('style.margin')
  get tappedMargin(): string | null {
    return !this.preview && this.permanent?.tapped ? '-33px 33px' : null;
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

  get displayLoyalty(): number | null {
    if (this.permanent && this.permanent.loyaltyCounters > 0) return this.permanent.loyaltyCounters;
    return this.card.loyalty ?? null;
  }

  private formatEnumName(s: string): string {
    return s.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ');
  }

  get typeLine(): string {
    const supertypes = (this.card.supertypes ?? []).map(s => this.formatEnumName(s));
    const mainType = [...supertypes, this.formatEnumName(this.card.type)].join(' ');
    const subtypes = (this.card.subtypes ?? []).map(s => this.formatEnumName(s));
    if (subtypes.length > 0) {
      return `${mainType} \u2014 ${subtypes.join(' ')}`;
    }
    return mainType;
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

  get textBoxFontSize(): string {
    let total = '';
    if (this.card.cardText) total += this.card.cardText;
    if (this.effectiveKeywords.length > 0) total += this.formatKeywords(this.effectiveKeywords);
    if (this.flavorText) total += this.flavorText;
    const len = total.length;
    if (len <= 50) return '11px';
    if (len <= 90) return '10.5px';
    if (len <= 140) return '10px';
    if (len <= 200) return '9.5px';
    return '9px';
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

  formatKeywords(keywords: string[]): string {
    return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase().replace('_', ' ')).join(', ');
  }
}
