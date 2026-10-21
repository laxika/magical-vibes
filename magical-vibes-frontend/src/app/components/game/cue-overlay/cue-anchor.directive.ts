import { Directive, ElementRef, effect, inject, input } from '@angular/core';
import { CueAnchorKind, CueElementRegistry } from '../../../services/cue-elements.service';
import { releaseCueHolds } from './cue-card-motion';

/**
 * Registers its element with the {@link CueElementRegistry} while it is rendered, so visual
 * effects can find it: `appCueAnchor="permanent" [cueAnchorId]="perm.id" [cueAnchorCardId]="perm.card.id"`.
 * When the element starts showing something else (a stack entry tracked by position), it lets go
 * of any frame an effect held on it for what it showed before.
 */
@Directive({
  selector: '[appCueAnchor]',
  standalone: true
})
export class CueAnchorDirective {
  readonly kind = input.required<CueAnchorKind>({ alias: 'appCueAnchor' });
  readonly id = input.required<string | null | undefined>({ alias: 'cueAnchorId' });
  readonly cardId = input<string | null | undefined>(null, { alias: 'cueAnchorCardId' });

  constructor() {
    const element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
    const registry = inject(CueElementRegistry);
    let shown: string | null = null;
    effect(onCleanup => {
      const id = this.id() ?? null;
      if (shown !== null && id !== shown) releaseCueHolds(element);
      shown = id;
      if (!id) return;
      onCleanup(registry.register(this.kind(), id, element, this.cardId() ?? null));
    });
  }
}
