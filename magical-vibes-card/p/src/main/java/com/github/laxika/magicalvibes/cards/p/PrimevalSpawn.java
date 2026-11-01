package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfIfUncastOrUnpaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMC", collectorNumber = "13")
@CardRegistration(set = "DMC", collectorNumber = "89")
public class PrimevalSpawn extends Card {

    public PrimevalSpawn() {
        addStaticEffect(new ExileSelfIfUncastOrUnpaidEffect());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                ExileTopCardsAndMayCastSpellsEffect.controllerWithTotalManaValue(10, 10));
    }
}
