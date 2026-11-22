package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "31")
public class ArbiterOfTheIdeal extends Card {

    public ArbiterOfTheIdeal() {
        var match = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT), new CardTypePredicate(CardType.CREATURE), new CardTypePredicate(CardType.LAND)));
        addTrigger(Triggers.becomesUntapped(),
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.REVEAL, match),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(match))
                                .withCounters(CounterType.MANIFESTATION, 1)
                                .enteringWith(new GrantCardTypeEffect(CardType.ENCHANTMENT, Subjects.target())),
                        "Put the revealed card onto the battlefield?")));
    }
}
