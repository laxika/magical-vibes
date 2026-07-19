import { Injectable, signal } from '@angular/core';
import { Card, Permanent } from './websocket.service';

/**
 * Media query for the game screen's phone layout: portrait phones (narrow
 * viewport) plus small coarse-pointer landscape screens. The CSS phone blocks
 * (game-phone.css, side-panel.component.css) mirror these breakpoints — keep
 * them in sync when changing.
 */
export const PHONE_LAYOUT_MEDIA =
  '(max-width: 700px), ((pointer: coarse) and (max-width: 1024px) and (max-height: 500px))';

/**
 * Holds the card shown by the phone long-press preview overlay. On phones the
 * side panel's hover preview is hidden (no hover on touch), so card-display
 * components push their card here while a finger holds them down and the game
 * screen renders it as a fullscreen overlay.
 */
@Injectable({ providedIn: 'root' })
export class CardPreviewService {
  readonly previewCard = signal<Card | null>(null);
  readonly previewPermanent = signal<Permanent | null>(null);

  isPhoneLayout(): boolean {
    return typeof window !== 'undefined' && window.matchMedia(PHONE_LAYOUT_MEDIA).matches;
  }

  show(card: Card, permanent: Permanent | null = null): void {
    this.previewCard.set(card);
    this.previewPermanent.set(permanent);
  }

  clear(): void {
    this.previewCard.set(null);
    this.previewPermanent.set(null);
  }
}
