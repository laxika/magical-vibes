import { Directive, ElementRef, effect, inject, input } from '@angular/core';
import { CueAnchorKind, CueElementRegistry } from '../../../services/cue-elements.service';

/**
 * Registers its element with the {@link CueElementRegistry} while it is rendered, so visual
 * effects can find it: `appCueAnchor="permanent" [cueAnchorId]="perm.id" [cueAnchorCardId]="perm.card.id"`.
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
    effect(onCleanup => {
      const id = this.id();
      if (!id) return;
      onCleanup(registry.register(this.kind(), id, element, this.cardId() ?? null));
    });
  }
}
