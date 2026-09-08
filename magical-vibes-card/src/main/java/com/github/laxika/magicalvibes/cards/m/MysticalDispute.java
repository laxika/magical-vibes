package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "58")
public class MysticalDispute extends Card {

    public MysticalDispute() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingStackEntryEffect(
                new StackEntryColorInPredicate(Set.of(CardColor.BLUE)), 2));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(3));
    }
}
