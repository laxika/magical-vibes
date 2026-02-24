package com.github.laxika.magicalvibes.model.effect;

public record RevealTopCardDealManaValueDamageEffect(
        boolean damageTargetPlayer,
        boolean damageTargetCreatures,
        boolean returnToHandIfLand
) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
