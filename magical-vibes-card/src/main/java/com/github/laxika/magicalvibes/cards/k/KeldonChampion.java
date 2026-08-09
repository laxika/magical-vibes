package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "UDS", collectorNumber = "90")
public class KeldonChampion extends Card {

    public KeldonChampion() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                "Target must be a player or planeswalker"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetPlayerOrPlaneswalkerEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterEchoAtNextUpkeepEffect("{2}{R}{R}"));
    }
}
