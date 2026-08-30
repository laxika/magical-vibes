package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "32")
public class Skyscribing extends Card {

    public Skyscribing() {
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(new XValue()));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new EachPlayerDrawsCardEffect(1)),
                "Forecast — {2}{U}, Reveal this card from your hand: Each player draws a card. "
                        + "Activate only during your upkeep and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
