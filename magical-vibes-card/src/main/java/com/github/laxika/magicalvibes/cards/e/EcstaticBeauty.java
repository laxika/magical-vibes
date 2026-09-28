package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "83")
public class EcstaticBeauty extends Card {

    public EcstaticBeauty() {
        addEffect(EffectSlot.SPELL,
                new ExileTopCardsMayPlayThisTurnAndPutTimeCountersOnSuspendedCardsEffect(3, 4));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(),
                "Suspend 4—{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
