package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfIfUncastOrUnpaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;

@CardRegistration(set = "DMC", collectorNumber = "13")
@CardRegistration(set = "DMC", collectorNumber = "89")
public class PrimevalSpawn extends Card {

    public PrimevalSpawn() {
        addEffect(EffectSlot.STATIC, new ExileSelfIfUncastOrUnpaidEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                ExileTopCardsAndMayCastSpellsEffect.controllerWithTotalManaValue(10, 10));
    }
}
