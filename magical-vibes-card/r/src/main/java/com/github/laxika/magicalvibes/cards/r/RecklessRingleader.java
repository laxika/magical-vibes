package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "J21", collectorNumber = "21")
public class RecklessRingleader extends Card {

    public RecklessRingleader() {
        // When this creature enters, choose a creature card in your hand. It perpetually gains haste.
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ChooseCardEffect.oneOf(Subjects.cardsInYourHand(new CardTypePredicate(CardType.CREATURE)),
                        "a creature card in your hand"),
                PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(Subjects.cardsThisWay(StepKind.CHOOSE),
                        Keyword.HASTE)));
    }
}
