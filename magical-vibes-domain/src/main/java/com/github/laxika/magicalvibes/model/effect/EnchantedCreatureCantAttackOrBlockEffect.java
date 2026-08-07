package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect: the enchanted creature is barred outright from attacking, from blocking, or
 * from both. Unlike {@link EnchantedCreatureCombatTaxEffect} no payment lifts this — the declaration
 * is simply illegal.
 *
 * <p>The two flags select which halves of the prohibition apply, so the record covers both the
 * Pacifism wording ("can't attack or block") and the attack-only wording ("can't attack", Forced
 * Worship), which still leaves the creature free to block. A block-only Aura has no printed card
 * today but is expressible. At least one flag must be set — an all-false value would be a static
 * effect that restricts nothing, so the compact constructor rejects it.
 *
 * <p>Read directly with no handler, via {@code GameQueryService.hasAuraWithEffect} with a matcher
 * that also inspects the relevant flag: {@code AttackLegalityService.canAttack} reads
 * {@code preventsAttacking}, {@code BlockLegalityService.hasUnmetBlockRequirement} reads
 * {@code preventsBlocking}.
 */
public record EnchantedCreatureCantAttackOrBlockEffect(boolean preventsAttacking,
                                                       boolean preventsBlocking) implements CardEffect {

    public EnchantedCreatureCantAttackOrBlockEffect {
        if (!preventsAttacking && !preventsBlocking) {
            throw new IllegalArgumentException(
                    "EnchantedCreatureCantAttackOrBlockEffect must prevent attacking, blocking, or both");
        }
    }

    /** The Pacifism form: the enchanted creature can neither attack nor block. */
    public EnchantedCreatureCantAttackOrBlockEffect() {
        this(true, true);
    }
}
