package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top X cards of the target opponent's library, then let the controller cast any number
 * of the exiled spells with mana value X or less without paying their mana costs. Cards not cast
 * this way remain exiled.
 */
public record VillainousWealthEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
