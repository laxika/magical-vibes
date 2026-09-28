package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "51")
public class FieryEncore extends Card {

    public FieryEncore() {
        target(targetFilter())
                .addEffect(EffectSlot.SPELL, new DiscardCardThenEffect(
                        null,
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                new LastDiscardedCardManaValue()),
                        "a card",
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        true,
                        null,
                        null))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }

    private static PermanentPredicateTargetFilter targetFilter() {
        return new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be a creature or planeswalker");
    }
}
