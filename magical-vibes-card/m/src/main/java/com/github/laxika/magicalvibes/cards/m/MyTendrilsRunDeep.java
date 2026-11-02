package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "345")
public class MyTendrilsRunDeep extends Card {

    public MyTendrilsRunDeep() {
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                SequenceEffect.of(new DrawCardEffect(2), new SacrificeSelfEffect())));
    }
}
