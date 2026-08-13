package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentOfSameTypeEffect;

@CardRegistration(set = "CHR", collectorNumber = "19")
@CardRegistration(set = "USG", collectorNumber = "72")
public class EnchantmentAlteration extends Card {

    public EnchantmentAlteration() {
        addEffect(EffectSlot.SPELL, new AttachTargetAuraToAnotherPermanentOfSameTypeEffect());
    }
}
