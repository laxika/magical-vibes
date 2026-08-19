package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "RIX", collectorNumber = "115")
public class SilvercladFerocidons extends Card {

    public SilvercladFerocidons() {
        // Enrage — Whenever this creature is dealt damage, each opponent sacrifices a permanent
        // of their choice.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.EACH_OPPONENT));
    }
}
