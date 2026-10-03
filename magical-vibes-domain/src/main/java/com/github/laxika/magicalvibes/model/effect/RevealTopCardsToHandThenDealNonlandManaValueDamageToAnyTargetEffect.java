package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top two cards of the controller's library, puts them into their hand, and, if at
 * least one is a nonland card, deals damage to the effect's any-target equal to the total mana
 * value of the revealed nonland cards.
 */
public record RevealTopCardsToHandThenDealNonlandManaValueDamageToAnyTargetEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
