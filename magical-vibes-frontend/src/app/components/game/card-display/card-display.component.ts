import { Component, Input, HostBinding } from '@angular/core';
import { Card, Permanent } from '../../../services/websocket.service';

@Component({
  selector: 'app-card-display',
  standalone: true,
  templateUrl: './card-display.component.html',
  styleUrl: './card-display.component.css',
  host: { 'class': 'card' }
})
export class CardDisplayComponent {
  @Input({ required: true }) card!: Card;
  @Input() permanent: Permanent | null = null;

  @HostBinding('attr.data-card-color')
  get cardColor(): string {
    return this.card.color;
  }

  get effectiveKeywords(): string[] {
    if (this.permanent) {
      const all = [...this.card.keywords];
      if (this.permanent.grantedKeywords) {
        for (const kw of this.permanent.grantedKeywords) {
          if (!all.includes(kw)) {
            all.push(kw);
          }
        }
      }
      return all;
    }
    return this.card.keywords;
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
    return [...supertypes, this.card.type].join(' ');
  }

  formatKeywords(keywords: string[]): string {
    return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase().replace('_', ' ')).join(', ');
  }
}
