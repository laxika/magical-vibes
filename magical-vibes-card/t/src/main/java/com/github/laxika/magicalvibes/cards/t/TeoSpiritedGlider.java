package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "74")
public class TeoSpiritedGlider extends Card {

    public TeoSpiritedGlider() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new HasAttacker(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new DiscardCardThenEffect(
                                        null,
                                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                                new PermanentAllOfPredicate(List.of(
                                                        new PermanentIsCreaturePredicate(),
                                                        new PermanentControlledBySourceControllerPredicate()))),
                                        "a card",
                                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))))));
    }
}
