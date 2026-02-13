import { Component, Input, HostBinding, OnInit, OnChanges, SimpleChanges, inject } from '@angular/core';
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

  artUrl: string | null = null;

  private scryfallImageService = inject(ScryfallImageService);

  ngOnInit(): void {
    this.fetchCardArt();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['card'] && !changes['card'].firstChange) {
      if (this.card.setCode && this.card.collectorNumber) {
        const cached = this.scryfallImageService.getCachedArtCropUrl(this.card.setCode, this.card.collectorNumber);
        if (cached) {
          this.artUrl = cached;
        } else {
          this.fetchCardArt();
        }
      } else {
        this.artUrl = null;
      }
    }
  }

  private fetchCardArt(): void {
    if (this.card.setCode && this.card.collectorNumber) {
      this.scryfallImageService.getArtCropUrl(this.card.setCode, this.card.collectorNumber)
        .then(url => this.artUrl = url)
        .catch(() => { this.artUrl = null; });
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

  get typeLine(): string {
    const supertypes = (this.card.supertypes ?? []).map(s => s.charAt(0) + s.slice(1).toLowerCase());
    const mainType = [...supertypes, this.card.type].join(' ');
    const subtypes = (this.card.subtypes ?? []).map(s => s.charAt(0) + s.slice(1).toLowerCase());
    if (subtypes.length > 0) {
      return `${mainType} \u2014 ${subtypes.join(' ')}`;
    }
    return mainType;
  }

  formatKeywords(keywords: string[]): string {
    return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase().replace('_', ' ')).join(', ');
  }
}
