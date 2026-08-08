package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "DGM", collectorNumber = "59")
public class BredForTheHunt extends Card {

    public BredForTheHunt() {
        // Whenever a creature you control with a +1/+1 counter on it deals combat damage to a
        // player, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));
    }
}
