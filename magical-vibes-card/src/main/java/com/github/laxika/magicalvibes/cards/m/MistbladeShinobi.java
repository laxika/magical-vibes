package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BOK", collectorNumber = "43")
public class MistbladeShinobi extends Card {

    public MistbladeShinobi() {
        // Ninjutsu {U}
        addNinjutsu("{U}");

        // Whenever this creature deals combat damage to a player, you may return target
        // creature that player controls to its owner's hand.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ReturnPermanentsOnCombatDamageToPlayerEffect(new PermanentIsCreaturePredicate(), 1));
    }
}
