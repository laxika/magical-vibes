package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersAndGainSuspendEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "85")
@CardRegistration(set = "MSC", collectorNumber = "406")
public class KangPrime extends Card {

    public KangPrime() {
        CardEffect effect = SequenceEffect.of(
                ExileCardsFromTopUntilEffect.untilNonland(Subjects.you()),
                new PutTimeCountersAndGainSuspendEffect(
                        Subjects.cardsThisWay(StepKind.EXILE, ExileCardsFromTopUntilEffect.NONLAND), 2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addTrigger(Triggers.attacks(), effect);
    }
}
