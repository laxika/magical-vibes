package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect;

@CardRegistration(set = "WOE", collectorNumber = "137")
public class ImodaneThePyrohammer extends Card {

    public ImodaneThePyrohammer() {
        addEffect(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE,
                new DealDamageToEachOpponentWhenSingleTargetCreatureSpellDealsDamageEffect());
    }
}
