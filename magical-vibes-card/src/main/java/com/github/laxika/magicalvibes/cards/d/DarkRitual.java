package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "5ED", collectorNumber = "153")
@CardRegistration(set = "4ED", collectorNumber = "129")
@CardRegistration(set = "ICE", collectorNumber = "120")
@CardRegistration(set = "MIR", collectorNumber = "116")
@CardRegistration(set = "TMP", collectorNumber = "118")
@CardRegistration(set = "TPR", collectorNumber = "93")
@CardRegistration(set = "ITP", collectorNumber = "19")
@CardRegistration(set = "RQS", collectorNumber = "18")
@CardRegistration(set = "USG", collectorNumber = "127")
public class DarkRitual extends Card {

    public DarkRitual() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.BLACK, 3));
    }
}
