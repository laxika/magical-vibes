package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ANB", collectorNumber = "48")
public class DemonOfLoathing extends Card {

    public DemonOfLoathing() {
        // Whenever this creature deals combat damage to a player, that player sacrifices a creature
        // of their choice.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.TARGET_PLAYER));
    }
}
