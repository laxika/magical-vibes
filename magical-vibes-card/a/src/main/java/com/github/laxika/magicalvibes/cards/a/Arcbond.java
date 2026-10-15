package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealtDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "91")
public class Arcbond extends Card {

    public Arcbond() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RegisterDelayedWatchedCreatureDealtDamageEffect(
                        List.of(new MassDamageEffect(
                                new EventValue(), true, false,
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))));
    }
}
