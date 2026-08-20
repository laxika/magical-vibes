package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect: "As an additional cost to cast this spell, pay X life." (Fire Covenant), or
 * "Pay X life" as an activated-ability cost.
 *
 * <p>X is the value announced as the spell is cast (CR 601.2b) — the spell's mana cost need not
 * contain {X}. The payment is legal only while the caster's life total is at least X (CR 119.4);
 * validated and paid at cast time in {@code SpellCastingService}.
 */
public record PayXLifeCost() implements CostEffect {
}
