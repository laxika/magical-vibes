package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "163")
@CardRegistration(set = "OM1", collectorNumber = "164")
@CardRegistration(set = "MSC", collectorNumber = "824")
public class EerieGravestone extends Card {

    public EerieGravestone() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        SequenceEffect.of(
                                new MillEffect(4, Subjects.you()),
                                ChooseCardEffect.upTo(
                                        Subjects.cardsThisWay(StepKind.MILL, new CardTypePredicate(CardType.CREATURE)), 1,
                                        "a creature card milled this way"),
                                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))),
                "{1}{B}, Sacrifice this artifact: Mill four cards. You may put a creature card from among them into your hand."
        ));
    }
}
