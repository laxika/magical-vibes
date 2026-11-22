package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class RonaTolarianObliterator extends Card {

    private static final CardPredicate LAND = new CardTypePredicate(CardType.LAND);

    public RonaTolarianObliterator() {
        addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,
                ExileCardAtRandomEffect.fromHand(Subjects.controllerOf(Subjects.triggering())),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, LAND),
                        new MayEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.EXILE, LAND))
                                .underControlOf(Subjects.you()),
                                "Put the exiled land card onto the battlefield under your control?")),
                new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(StepKind.EXILE)));
    }
}
