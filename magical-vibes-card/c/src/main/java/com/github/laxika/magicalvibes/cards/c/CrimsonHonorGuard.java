package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOC", collectorNumber = "145")
@CardRegistration(set = "C17", collectorNumber = "23")
public class CrimsonHonorGuard extends Card {

    public CrimsonHonorGuard() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new NotCondition(new TargetPlayerControlsPermanent(new PermanentIsCommanderPredicate())),
                new DealDamageToEndStepPlayerEffect(new Fixed(4))));
    }
}
