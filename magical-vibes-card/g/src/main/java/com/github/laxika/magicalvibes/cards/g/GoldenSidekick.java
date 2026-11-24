package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDSK", collectorNumber = "23")
public class GoldenSidekick extends Card {

    public GoldenSidekick() {
        addTrigger(Triggers.youGainLife(),
                SequenceEffect.of(
                        ChooseCardEffect.atRandom(Subjects.cardsInYourHand(new CardTypePredicate(CardType.CREATURE)),
                                "a creature card"),
                        new PerpetuallyBoostCardEffect(null, new EventValue(), new EventValue(),
                                Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
