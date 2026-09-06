package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlledDragonAsCast;
import com.github.laxika.magicalvibes.model.condition.RevealCardFromHandCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "78")
public class SilumgarsScorn extends Card {

    public SilumgarsScorn() {
        addEffect(EffectSlot.SPELL, RevealCardFromHandCost.optional(
                new CardSubtypePredicate(CardSubtype.DRAGON), "Dragon"));

        Condition dragonBonus = new AnyOf(List.of(
                new RevealCardFromHandCostPaid(),
                new ControlledDragonAsCast()));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                dragonBonus,
                new CounterUnlessPaysEffect(1),
                new CounterSpellEffect()));
    }
}
