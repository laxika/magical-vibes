package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "JUD", collectorNumber = "91")
public class GoretuskFirebeast extends Card {

    public GoretuskFirebeast() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetPlayerOrPlaneswalkerEffect(4));
    }
}
