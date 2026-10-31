package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraToAnotherPermanentOfSameTypeEffect;

@CardRegistration(set = "CHR", collectorNumber = "19")
@CardRegistration(set = "USG", collectorNumber = "72")
@CardRegistration(set = "LEG", collectorNumber = "53")
public class EnchantmentAlteration extends Card {

    public EnchantmentAlteration() {
        addSpellEffect(new AttachTargetAuraToAnotherPermanentOfSameTypeEffect());
    }
}
