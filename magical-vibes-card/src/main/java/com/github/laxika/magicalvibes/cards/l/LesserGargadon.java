package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "199")
public class LesserGargadon extends Card {

    public LesserGargadon() {
        // Whenever this creature attacks or blocks, sacrifice a land.
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_BLOCK, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
    }
}
