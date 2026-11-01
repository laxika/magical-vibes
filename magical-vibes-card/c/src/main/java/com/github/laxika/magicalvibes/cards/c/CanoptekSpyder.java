package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "151")
public class CanoptekSpyder extends Card {

    public CanoptekSpyder() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardNotPredicate(new CardIsTokenPredicate()))),
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardSubtypePredicate(CardSubtype.VEHICLE),
                                        new CardNotPredicate(new CardIsTokenPredicate()))))),
                        new DrawCardEffect(1)));
    }
}
