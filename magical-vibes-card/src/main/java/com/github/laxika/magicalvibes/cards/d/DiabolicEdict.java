package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TMP", collectorNumber = "128")
@CardRegistration(set = "TPR", collectorNumber = "102")
public class DiabolicEdict extends Card {

    public DiabolicEdict() {
        // Target player sacrifices a creature of their choice.
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1,
                new PermanentIsCreaturePredicate(),
                SacrificeRecipient.TARGET_PLAYER));
    }
}
