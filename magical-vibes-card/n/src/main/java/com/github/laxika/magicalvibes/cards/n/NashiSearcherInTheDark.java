package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "223")
public class NashiSearcherInTheDark extends Card {

    public NashiSearcherInTheDark() {
        CardAnyOfPredicate legendaryOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(
                        new MillEffect(new EventValue(), Subjects.you()),
                        ChooseCardEffect.upTo(
                                Subjects.cardsThisWay(StepKind.MILL, legendaryOrEnchantment), Integer.MAX_VALUE,
                                "legendary and/or enchantment cards milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(new NotCondition(new HappenedThisWay(StepKind.CHOOSE)),
                                new PutCountersOnSourceEffect(1, 1, 1))));
    }
}
