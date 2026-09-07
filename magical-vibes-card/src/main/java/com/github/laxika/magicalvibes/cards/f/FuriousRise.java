package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilAnotherEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "M21", collectorNumber = "144")
@CardRegistration(set = "THB", collectorNumber = "136")
public class FuriousRise extends Card {

    public FuriousRise() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4)),
                new ExileTopCardMayPlayUntilAnotherEffect()));
    }
}
