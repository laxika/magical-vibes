package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "109")
public class WickedGuardian extends Card {

    public WickedGuardian() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, Subjects.otherCreaturesYouControl(),
                                "another creature you control"),
                        DealDamageEffect.to(Subjects.thisWay(StepKind.CHOOSE), 2),
                        new ConditionalEffect(new HappenedThisWay(StepKind.CHOOSE), new DrawCardEffect())),
                        "Have it deal 2 damage to another creature you control?"));
    }
}
