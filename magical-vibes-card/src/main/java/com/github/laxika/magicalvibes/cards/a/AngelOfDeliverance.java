package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "2")
public class AngelOfDeliverance extends Card {

    public AngelOfDeliverance() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE,
                        new ConditionalEffect(new Delirium(), new ExileTargetPermanentEffect()));
    }
}
