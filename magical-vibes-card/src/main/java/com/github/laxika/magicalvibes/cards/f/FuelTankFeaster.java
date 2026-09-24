package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect;

@CardRegistration(set = "YDFT", collectorNumber = "17")
public class FuelTankFeaster extends Card {

    public FuelTankFeaster() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect(1));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
