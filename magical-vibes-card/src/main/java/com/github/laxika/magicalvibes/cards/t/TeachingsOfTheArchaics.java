package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "57")
public class TeachingsOfTheArchaics extends Card {

    public TeachingsOfTheArchaics() {
        AnOpponentHasMoreCardsInHandThanController fourMore =
                new AnOpponentHasMoreCardsInHandThanController(4);
        addEffect(EffectSlot.SPELL, new ConditionalEffect(fourMore, new DrawCardEffect(3)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AllConditions(List.of(
                        new AnOpponentHasMoreCardsInHandThanController(),
                        new NotCondition(fourMore))),
                new DrawCardEffect(2)));
    }
}
