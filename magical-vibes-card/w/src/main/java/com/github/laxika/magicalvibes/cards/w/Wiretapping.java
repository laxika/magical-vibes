package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstDrawStepDrawOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "65")
public class Wiretapping extends Card {

    public Wiretapping() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ImprintFromTopCardsEffect(5, true));
        addTrigger(Triggers.youDrawCard(), new FirstDrawStepDrawOnlyTriggerEffect(SequenceEffect.of(
                new DrawCardEffect(1),
                new ConditionalEffect(new CardsInHandAtLeast(9),
                        new PlayImprintedCardWithoutPayingManaCostEffect())
        )));
    }
}
