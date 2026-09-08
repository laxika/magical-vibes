package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "187")
public class WheelOfFate extends Card {

    public WheelOfFate() {
        addEffect(EffectSlot.SPELL, new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(),
                "Suspend 4\u2014{1}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
