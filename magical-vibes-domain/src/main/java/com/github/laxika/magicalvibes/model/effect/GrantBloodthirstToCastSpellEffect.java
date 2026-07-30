package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants bloodthirst N to the spell that caused this trigger while it is still on the stack
 * (Bloodlord of Vaasgoth: "Whenever you cast a Vampire creature spell, it gains bloodthirst 3.").
 * <p>
 * Bloodthirst is a static ability (CR 702.54a): "If an opponent was dealt damage this turn, this
 * permanent enters with N +1/+1 counters on it." The grant is therefore recorded on the spell's
 * {@code StackEntry.grantedBloodthirst}, carried onto the entering permanent and turned into
 * counters by the as-enters replacement, not by putting counters on afterwards. Multiple grants
 * accumulate because each instance of bloodthirst applies separately (CR 702.54c).
 * <p>
 * Place in {@code EffectSlot.ON_CONTROLLER_CASTS_SPELL} inside a {@code SpellCastTriggerEffect}.
 */
public record GrantBloodthirstToCastSpellEffect(int amount) implements CardEffect {
}
