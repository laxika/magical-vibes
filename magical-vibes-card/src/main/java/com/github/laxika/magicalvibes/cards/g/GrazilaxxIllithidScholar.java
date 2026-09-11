package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "AFR", collectorNumber = "60")
public class GrazilaxxIllithidScholar extends Card {

    public GrazilaxxIllithidScholar() {
        // Whenever a creature you control becomes blocked, you may return it to its owner's hand.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED,
                new MayEffect(ReturnToHandEffect.self(), "Return that creature to its owner's hand?"));

        // Whenever one or more creatures you control deal combat damage to a player, draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new DrawCardEffect(1), false, true));
    }
}
