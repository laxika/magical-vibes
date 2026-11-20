package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "190")
@CardRegistration(set = "DMC", collectorNumber = "154")
public class IllunaApexOfWishes extends Card {

    public IllunaApexOfWishes() {
        CardPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                ExileCardsFromTopUntilEffect.NONLAND, new CardIsPermanentPredicate()));
        CardSubject thatCard = Subjects.cardsThisWay(StepKind.EXILE, nonlandPermanent);
        addTrigger(Triggers.mutates(), SequenceEffect.of(
                new ExileCardsFromTopUntilEffect(Subjects.you(), nonlandPermanent),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, nonlandPermanent),
                        new MayEffect(ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(thatCard),
                                "Put that card onto the battlefield? (Otherwise it goes into your hand.)",
                                new PutCardsIntoHandEffect(thatCard)))));
    }
}
