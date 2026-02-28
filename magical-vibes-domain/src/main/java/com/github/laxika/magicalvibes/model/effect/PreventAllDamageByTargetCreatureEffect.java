package com.github.laxika.magicalvibes.model.effect;

public record PreventAllDamageByTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
