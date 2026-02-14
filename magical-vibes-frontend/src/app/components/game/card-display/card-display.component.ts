import { Component, Input, HostBinding, OnInit, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { Card, Permanent } from '../../../services/websocket.service';
import { ScryfallImageService } from '../../../services/scryfall-image.service';

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

  artUrl = signal<string | null>(null);

  private scryfallImageService = inject(ScryfallImageService);

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

  get displayPower(): number | null {
    if (this.card.power == null) return null;
    return this.permanent ? this.permanent.effectivePower : this.card.power;
  }

  get displayToughness(): number | null {
    if (this.card.toughness == null) return null;
    return this.permanent ? this.permanent.effectiveToughness : this.card.toughness;
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

  get textBoxFontSize(): string {
    let total = '';
    if (this.card.cardText) total += this.card.cardText;
    if (this.effectiveKeywords.length > 0) total += this.formatKeywords(this.effectiveKeywords);
    if (this.card.flavorText) total += this.card.flavorText;
    const len = total.length;
    if (len <= 50) return '8px';
    if (len <= 90) return '7.5px';
    if (len <= 140) return '7px';
    if (len <= 200) return '6.5px';
    return '6px';
  }

  formatKeywords(keywords: string[]): string {
    return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase().replace('_', ' ')).join(', ');
  }
}
