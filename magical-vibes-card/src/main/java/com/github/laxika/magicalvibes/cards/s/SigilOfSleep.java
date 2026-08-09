package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "46")
public class SigilOfSleep extends Card {

    public SigilOfSleep() {
        target(TargetFilters.creature())
                // Whenever enchanted creature deals damage to a player, return target creature that
                // player controls to its owner's hand.
                .addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                        new ReturnPermanentControlledByPlayerToHandEffect(
                                new PermanentIsCreaturePredicate(), "creature", true));
    }
}
