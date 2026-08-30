package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreatureDealsDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "14")
public class PaladinOfPrahv extends Card {

    public PaladinOfPrahv() {
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new RegisterDelayedWatchedCreatureDealsDamageEffect(
                        List.of(new GainLifeEffect(new EventValue())))),
                "Forecast — {1}{W}, Reveal this card from your hand: Whenever target creature deals damage this turn, "
                        + "you gain that much life. Activate only during your upkeep and only once each turn.",
                TargetFilters.creature(),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
