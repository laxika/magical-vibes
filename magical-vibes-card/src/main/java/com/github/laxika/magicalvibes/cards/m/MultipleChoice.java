package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerThenReturnCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "48")
public class MultipleChoice extends Card {

    public MultipleChoice() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                exactXOrAtLeastFour(1), SequenceEffect.of(new ScryEffect(1), new DrawCardEffect(1))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                exactXOrAtLeastFour(2), new MayEffect(
                        new ChoosePlayerThenReturnCreatureToHandEffect(),
                        "Choose a player to return a creature?")));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                exactXOrAtLeastFour(3), new CreateTokenEffect(
                        "Elemental", 4, 4, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL))));
    }

    private static Condition exactXOrAtLeastFour(int value) {
        return new AnyOf(List.of(
                new AllConditions(List.of(
                        new SpellXAtLeast(value),
                        new NotCondition(new SpellXAtLeast(value + 1)))),
                new SpellXAtLeast(4)));
    }
}
