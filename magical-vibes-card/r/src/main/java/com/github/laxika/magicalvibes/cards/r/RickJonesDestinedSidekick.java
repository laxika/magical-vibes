package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "184")
public class RickJonesDestinedSidekick extends Card {

    public RickJonesDestinedSidekick() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(SequenceEffect.of(
                        new MillEffect(4, Subjects.you()),
                        ChooseCardEffect.upTo(
                                Subjects.cardsThisWay(StepKind.MILL, new CardAnyOfPredicate(List.of(
                                                new CardSubtypePredicate(CardSubtype.HERO),
                                        new CardTypePredicate(CardType.ENCHANTMENT)))), 1,
                                "a Hero or enchantment card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))),
                "{3}, {T}: Mill four cards. You may put a Hero or enchantment card from among those cards into your hand."
        ));
    }
}
