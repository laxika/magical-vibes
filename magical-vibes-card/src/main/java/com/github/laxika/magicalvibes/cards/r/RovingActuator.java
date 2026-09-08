package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "157")
public class RovingActuator extends Card {

    public RovingActuator() {
        CardPredicate instantOrSorceryWithManaValueAtMostTwo = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                new CardMaxManaValuePredicate(2)));
        ExileTargetCardFromGraveyardAndMayCastCopyEffect exileAndCopy =
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        instantOrSorceryWithManaValueAtMostTwo,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD);

        target(new GraveyardCardPredicateTargetFilter(
                instantOrSorceryWithManaValueAtMostTwo,
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ConditionalEffect(new VoidCondition(), exileAndCopy));
    }
}
