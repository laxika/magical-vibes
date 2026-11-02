package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "137")
@CardRegistration(set = "WOE", collectorNumber = "348")
public class ImodaneThePyrohammer extends Card {

    public ImodaneThePyrohammer() {
        addTrigger(Triggers.instantOrSorceryYouControlDealsDamage(),
                new DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect());
    }
}
