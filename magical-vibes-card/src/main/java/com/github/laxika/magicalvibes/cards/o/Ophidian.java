package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WTH", collectorNumber = "45")
public class Ophidian extends Card {

    public Ophidian() {
        // Whenever this creature attacks and isn't blocked, you may draw a card.
        // If you do, this creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(SequenceEffect.of(
                        new DrawCardEffect(1),
                        new AssignNoCombatDamageEffect()),
                        "You may draw a card. If you do, this creature assigns no combat damage this turn."));
    }
}
