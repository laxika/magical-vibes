package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "13")
public class UnexpectedConversion extends Card {

    public UnexpectedConversion() {
        addSpellEffect(new DrawCardEffect(2));
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));
        // "For each card exiled from your hand this way": the first card and those the search exiled from the hand.
        addSpellEffect(SequenceEffect.of(
                ExileCardFromHandEffect.upToOne(instantOrSorcery),
                new ConditionalEffect(HappenedThisWay.exiled(), SequenceEffect.of(
                        ExileCardsWithSameNameEffect.anyNumber(Subjects.cardsThisWay(StepKind.EXILE))
                                .searching(Subjects.you(), Zone.HAND, Zone.LIBRARY),
                        new SeekCardsToHandEffect(
                                new Sum(AmountThisWay.exiledFromHand(), new Fixed(1)), instantOrSorcery)))));
    }
}
