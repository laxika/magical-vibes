package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtCombatDamageToPlayerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "296")
@CardRegistration(set = "MB2", collectorNumber = "532")
public class NightOfTheFlyingMerfolk extends Card {

    public NightOfTheFlyingMerfolk() {
        setBedtimeStory(true);

        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                2, "Merfolk", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.MERFOLK), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new PutCounterOnEachControlledPermanentEffect(
                CounterType.FLYING, 1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsTappedPredicate()))));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DrawCardEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentDealtCombatDamageToPlayerThisTurnPredicate())),
                CountScope.CONTROLLER)));
    }
}
