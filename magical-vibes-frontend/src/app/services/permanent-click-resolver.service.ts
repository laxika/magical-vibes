import { inject, Injectable } from '@angular/core';
import { Permanent } from './websocket.service';
import { GameChoiceService } from './game-choice.service';

@Injectable({ providedIn: 'root' })
export class PermanentClickResolverService {

  private readonly choice = inject(GameChoiceService);

  /**
   * Attempts to resolve a permanent click via the shared choice dispatch chain
   * (choosingPermanent, choosingMultiplePermanents, multiTargeting,
   * distributingDamage, selectingTarget).
   *
   * Returns true if the click was consumed (caller should not run fallback logic).
   *
   * @param perm The clicked permanent (may be undefined if index is out of bounds).
   * @param damageFilter Optional predicate gate for distributingDamage.
   *   - Pass a predicate to conditionally allow damage assignment.
   *   - Omit (undefined) to skip the distributingDamage check entirely.
   */
  tryResolveClick(perm: Permanent | undefined, damageFilter?: (perm: Permanent) => boolean): boolean {
    if (this.choice.choosingPermanent) {
      if (perm && this.choice.choosablePermanentIds().has(perm.id)) {
        this.choice.choosePermanent(perm.id);
      }
      return true;
    }
    if (this.choice.choosingMultiplePermanents) {
      if (perm && this.choice.multiPermanentChoiceIds().has(perm.id)) {
        this.choice.toggleMultiPermanentSelection(perm.id);
      }
      return true;
    }
    if (this.choice.targeting.multiTargeting) {
      if (perm && this.choice.targeting.validTargetPermanentIds().has(perm.id)) {
        if (this.choice.targeting.isMultiTargetSelected(perm.id)) {
          this.choice.targeting.removeMultiTarget(perm.id);
        } else {
          this.choice.targeting.addMultiTarget(perm.id);
        }
      }
      return true;
    }
    if (damageFilter !== undefined && this.choice.damage.distributingDamage) {
      if (perm && damageFilter(perm)) {
        this.choice.damage.assignDamage(perm.id);
      }
      return true;
    }
    if (this.choice.targeting.selectingTarget) {
      if (perm && this.choice.targeting.isValidTarget(perm)) {
        this.choice.targeting.selectTarget(perm.id);
      }
      return true;
    }
    return false;
  }
}
