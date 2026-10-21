import { Injectable } from '@angular/core';
import { VisualCue } from './websocket.service';

/**
 * What an on-screen element stands for: a battlefield permanent (by permanent id), a player's
 * panel (by player id), or a stack entry (by its card id, which is all a stack entry has).
 */
export type CueAnchorKind = 'permanent' | 'player' | 'stack';

interface CueAnchor {
  element: HTMLElement;
  /** The card id a permanent shows, so a cue's source card can be found on the battlefield. */
  cardId: string | null;
}

/**
 * Where cue sources and targets are on screen. Permanents, player panels and stack entries
 * register their element while they are rendered (see `CueAnchorDirective`); effects ask for
 * the element's current rectangle when they draw.
 */
@Injectable({ providedIn: 'root' })
export class CueElementRegistry {
  private readonly anchors = new Map<string, CueAnchor[]>();

  /** Registers an element for a permanent, player or stack entry; returns the unregister call. */
  register(kind: CueAnchorKind, id: string, element: HTMLElement, cardId: string | null = null): () => void {
    const key = kind + ':' + id;
    const anchor: CueAnchor = { element, cardId };
    this.anchors.set(key, [...(this.anchors.get(key) ?? []), anchor]);
    return () => {
      const remaining = (this.anchors.get(key) ?? []).filter(a => a !== anchor);
      if (remaining.length > 0) {
        this.anchors.set(key, remaining);
      } else {
        this.anchors.delete(key);
      }
    };
  }

  /** The on-screen rectangle (viewport coordinates) of a registered element, or null if none shows. */
  rect(kind: CueAnchorKind, id: string): DOMRect | null {
    return CueElementRegistry.firstVisible(this.anchors.get(kind + ':' + id) ?? []);
  }

  /** Where a card is: the battlefield permanent showing it, else its stack entry. */
  cardRect(cardId: string): DOMRect | null {
    const permanents: CueAnchor[] = [];
    for (const [key, anchors] of this.anchors) {
      if (key.startsWith('permanent:')) {
        permanents.push(...anchors.filter(a => a.cardId === cardId));
      }
    }
    return CueElementRegistry.firstVisible(permanents) ?? this.rect('stack', cardId);
  }

  /** The cue's target on screen: its permanent, else its player. */
  targetRect(cue: VisualCue): DOMRect | null {
    if (cue.targetPermanentId) return this.rect('permanent', cue.targetPermanentId);
    if (cue.targetPlayerId) return this.rect('player', cue.targetPlayerId);
    return null;
  }

  /** The cue's source card on screen, or null when it is nowhere visible (a resolved spell). */
  sourceRect(cue: VisualCue): DOMRect | null {
    return cue.sourceCardId ? this.cardRect(cue.sourceCardId) : null;
  }

  private static firstVisible(anchors: CueAnchor[]): DOMRect | null {
    for (const { element } of anchors) {
      if (!element.isConnected) continue;
      const rect = element.getBoundingClientRect();
      if (rect.width > 0 && rect.height > 0) return rect;
    }
    return null;
  }
}
