import { Component, HostBinding, Input } from '@angular/core';
import { GrantedAbilityView, ModifierLine, Permanent } from '../../../services/websocket.service';
import { formatKeywords, formatTypeLine } from '../../../utils/format-utils';

interface BreakdownRow {
  label: string;
  value: string;
  tone: 'positive' | 'negative' | 'neutral';
}

interface ModifierBreakdown {
  /** Whether the permanent has P/T to show (creature or animated). */
  showPt: boolean;
  basePower: number;
  baseToughness: number;
  finalPower: number;
  finalToughness: number;
  rows: BreakdownRow[];
}

/**
 * Fixed-position tooltip anchored to a hovered battlefield card, showing the
 * permanent's static characteristics (name, type line, printed keywords, P/T,
 * loyalty) followed by the per-source modifier attribution when the permanent
 * is modified. The game component computes the anchor point from the hovered
 * card's bounding rect; `below` flips the tooltip under the card when there is
 * no room above it.
 */
@Component({
  selector: 'app-modifier-tooltip',
  standalone: true,
  templateUrl: './modifier-tooltip.component.html',
  styleUrl: './modifier-tooltip.component.css'
})
export class ModifierTooltipComponent {
  @Input({ required: true }) permanent!: Permanent;

  @HostBinding('style.left.px') @Input() x = 0;
  @HostBinding('style.top.px') @Input() y = 0;
  @HostBinding('class.below') @Input() below = false;

  get typeLine(): string {
    return formatTypeLine(this.permanent.card);
  }

  get printedKeywords(): string | null {
    const keywords = this.permanent.card.keywords;
    return keywords.length > 0 ? formatKeywords(keywords) : null;
  }

  get displayLoyalty(): number | null {
    const loyaltyCounters = this.permanent.counters['LOYALTY'] ?? 0;
    if (loyaltyCounters > 0) return loyaltyCounters;
    return this.permanent.card.loyalty ?? null;
  }

  get grantedAbilities(): GrantedAbilityView[] {
    return this.permanent.grantedAbilities ?? [];
  }

  // ========== Modifier breakdown ==========
  // Per-source attribution from permanent.modifierLines (layer 6/7 provenance computed by the
  // engine), reconciled against the layered wire aggregates so the rows always sum exactly to
  // the displayed P/T: base lines fold last-wins per component over the printed base, counters
  // come from the counters map, and anything un-attributed (one-shot pumps stored on the
  // permanent) lands in an "Other effects" remainder row. Rows are empty for an unmodified
  // permanent — the tooltip then shows just the static characteristics.

  get modifierBreakdown(): ModifierBreakdown {
    const p = this.permanent;
    const lines = p.modifierLines ?? [];
    const creatureLike = p.card.power != null || p.animatedCreature;

    const plusCounters = p.counters['PLUS_ONE_PLUS_ONE'] ?? 0;
    const minusCounters = p.counters['MINUS_ONE_MINUS_ONE'] ?? 0;
    const counterDelta = plusCounters - minusCounters;

    const baseLines = lines.filter(l => l.basePower != null || l.baseToughness != null);
    const switchLines = lines.filter(l => l.switchesPt);
    // Two switches cancel — the displayed effective P/T is swapped when the parity is odd,
    // so un-swap it to reconcile the additive rows.
    const switched = switchLines.length % 2 === 1;
    const preSwitchPower = switched ? p.effectiveToughness : p.effectivePower;
    const preSwitchToughness = switched ? p.effectivePower : p.effectiveToughness;

    let basePower: number;
    let baseToughness: number;
    if (baseLines.length > 0) {
      basePower = p.card.power ?? 0;
      baseToughness = p.card.toughness ?? 0;
      for (const l of baseLines) {
        if (l.basePower != null) basePower = l.basePower;
        if (l.baseToughness != null) baseToughness = l.baseToughness;
      }
    } else {
      // No base-setting effects: reconstruct the printed base from the aggregates
      // (also covers legacy paths the attribution doesn't know about).
      basePower = p.effectivePower - p.powerModifier - counterDelta;
      baseToughness = p.effectiveToughness - p.toughnessModifier - counterDelta;
    }

    const rows: BreakdownRow[] = [];

    for (const l of baseLines) {
      rows.push({ label: l.source, value: this.lineValue(l), tone: this.lineTone(l) });
    }
    if (plusCounters > 0) {
      rows.push({ label: `+1/+1 counters (${plusCounters})`, value: `+${plusCounters}/+${plusCounters}`, tone: 'positive' });
    }
    if (minusCounters > 0) {
      rows.push({ label: `−1/−1 counters (${minusCounters})`, value: `−${minusCounters}/−${minusCounters}`, tone: 'negative' });
    }

    let attributedPower = 0;
    let attributedToughness = 0;
    const attributedGained = new Set<string>();
    const attributedRemoved = new Set<string>();
    let anyLosesAll = false;
    for (const l of lines) {
      attributedPower += l.power;
      attributedToughness += l.toughness;
      l.gainedKeywords.forEach(k => attributedGained.add(k));
      l.removedKeywords.forEach(k => attributedRemoved.add(k));
      anyLosesAll = anyLosesAll || l.losesAllAbilities;
      if (baseLines.includes(l) || switchLines.includes(l)) continue;
      rows.push({ label: l.source, value: this.lineValue(l), tone: this.lineTone(l) });
    }

    // Un-attributed remainder: keeps the rows summing exactly to the displayed P/T.
    const remainderPower = creatureLike ? preSwitchPower - basePower - counterDelta - attributedPower : 0;
    const remainderToughness = creatureLike ? preSwitchToughness - baseToughness - counterDelta - attributedToughness : 0;
    const printed = p.card.keywords;
    const otherGained = p.grantedKeywords.filter(k => !printed.includes(k) && !attributedGained.has(k));
    const otherLost = anyLosesAll ? []
        : p.removedKeywords.filter(k => printed.includes(k) && !attributedRemoved.has(k));
    if (remainderPower !== 0 || remainderToughness !== 0 || otherGained.length > 0 || otherLost.length > 0) {
      const parts: string[] = [];
      if (remainderPower !== 0 || remainderToughness !== 0) {
        parts.push(`${this.signed(remainderPower)}/${this.signed(remainderToughness)}`);
      }
      if (otherGained.length > 0) parts.push(formatKeywords(otherGained));
      if (otherLost.length > 0) parts.push('loses ' + formatKeywords(otherLost));
      const positive = remainderPower > 0 || remainderToughness > 0 || otherGained.length > 0;
      const negative = remainderPower < 0 || remainderToughness < 0 || otherLost.length > 0;
      rows.push({ label: 'Other effects', value: parts.join(', '), tone: positive && !negative ? 'positive' : negative && !positive ? 'negative' : 'neutral' });
    }

    for (const l of switchLines) {
      rows.push({ label: l.source, value: 'switches P/T', tone: 'neutral' });
    }

    return {
      showPt: creatureLike,
      basePower, baseToughness,
      finalPower: p.effectivePower,
      finalToughness: p.effectiveToughness,
      rows,
    };
  }

  private lineValue(l: ModifierLine): string {
    const parts: string[] = [];
    if (l.power !== 0 || l.toughness !== 0) {
      parts.push(`${this.signed(l.power)}/${this.signed(l.toughness)}`);
    }
    if (l.basePower != null || l.baseToughness != null) {
      parts.push(`base ${l.basePower ?? '—'}/${l.baseToughness ?? '—'}`);
    }
    if (l.gainedKeywords.length > 0) {
      parts.push(formatKeywords(l.gainedKeywords));
    }
    if (l.losesAllAbilities) {
      parts.push('loses all abilities');
    } else if (l.removedKeywords.length > 0) {
      parts.push('loses ' + formatKeywords(l.removedKeywords));
    }
    return parts.join(', ');
  }

  private lineTone(l: ModifierLine): 'positive' | 'negative' | 'neutral' {
    const positive = l.power > 0 || l.toughness > 0 || l.gainedKeywords.length > 0;
    const negative = l.power < 0 || l.toughness < 0 || l.removedKeywords.length > 0 || l.losesAllAbilities;
    if (positive && !negative) return 'positive';
    if (negative && !positive) return 'negative';
    return 'neutral';
  }

  private signed(n: number): string {
    return n < 0 ? `−${-n}` : `+${n}`;
  }
}
