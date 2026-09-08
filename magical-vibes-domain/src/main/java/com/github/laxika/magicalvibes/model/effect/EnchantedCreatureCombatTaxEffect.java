package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static Aura effect: the enchanted creature can't take the combat action named by {@code kind}
 * unless {@code amount} generic mana is paid as an additional cost to declare it. The declaration
 * itself stays legal — only the cost gates it, and per CR 508.1h / CR 509.1d the amount joins the
 * declaring player's total cost to attack or block.
 *
 * <p>Which player pays and how often the amount is charged is a property of {@link CombatTaxKind},
 * not of this record. All three kinds are read directly (there is no handler): the attack side is
 * summed in {@code CombatAttackService.declareAttackers} via
 * {@code GameQueryService.getCreatureAttackTax}, and both block sides in
 * {@code CombatBlockService.blockTaxFor}.
 *
 * <p>Contrast the uniform, defender-side {@link RequirePaymentToAttackEffect} (Windborn Muse), which
 * taxes every attacker rather than one enchanted creature, and the self-scoped
 * {@code CantAttackUnlessPaysPerCounterEffect}.
 */
public record EnchantedCreatureCombatTaxEffect(DynamicAmount amount, CombatTaxKind kind) implements CardEffect {

    public EnchantedCreatureCombatTaxEffect(int amount, CombatTaxKind kind) {
        this(new Fixed(amount), kind);
    }
}
