package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "43")
public class MonasterySiege extends Card {

    private static final String KHANS = "Khans";
    private static final String DRAGONS = "Dragons";

    public MonasterySiege() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseModeOnEnterEffect(List.of(KHANS, DRAGONS)));

        addEffect(EffectSlot.DRAW_TRIGGERED,
                new ConditionalEffect(new SourceHasChosenMode(KHANS),
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER))));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceHasChosenMode(DRAGONS),
                        IncreaseOpponentCostForTargetingControlledPermanentEffect
                                .forControllerAndControlledPermanents(2)));
    }
}
