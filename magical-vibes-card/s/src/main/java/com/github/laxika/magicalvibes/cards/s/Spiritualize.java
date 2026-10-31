package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealsDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "53")
public class Spiritualize extends Card {

    public Spiritualize() {
        // Until end of turn, whenever target creature deals damage, you gain that much life.
        target(TargetFilters.creature())
                .addSpellEffect(new RegisterDelayedWatchedCreatureDealsDamageEffect(
                        List.of(new GainLifeEffect(new EventValue()))))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
