package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "OGW", collectorNumber = "106")
public class DevourInFlames extends Card {

    public DevourInFlames() {
        addCastingCost(new ReturnPermanentToHandCost(new PermanentIsLandPredicate()));
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(5));
    }
}
