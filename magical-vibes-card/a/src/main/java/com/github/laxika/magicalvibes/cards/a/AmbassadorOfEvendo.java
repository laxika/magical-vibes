package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YEOE", collectorNumber = "22")
public class AmbassadorOfEvendo extends Card {

    public AmbassadorOfEvendo() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        ChooseCardEffect.atRandom(Subjects.cardsInYourLibrary(new CardTypePredicate(CardType.LAND)),
                                "a land card"),
                        new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                                Triggers.becomesTapped(), new DrawCardEffect())));
    }
}
