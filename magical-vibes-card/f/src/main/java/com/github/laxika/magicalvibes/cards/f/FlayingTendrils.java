package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingThisTurnEffect;

@CardRegistration(set = "OGW", collectorNumber = "70")
public class FlayingTendrils extends Card {

    public FlayingTendrils() {
        addSpellEffect(new BoostAllCreaturesEffect(-2, -2));
        addSpellEffect(new ExileCreaturesInsteadOfDyingThisTurnEffect());
    }
}
