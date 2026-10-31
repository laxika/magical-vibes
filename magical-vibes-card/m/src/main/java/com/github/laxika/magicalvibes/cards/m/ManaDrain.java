package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaEqualToTargetSpellManaValueEffect;

@CardRegistration(set = "LEG", collectorNumber = "65")
@CardRegistration(set = "VMA", collectorNumber = "78")
@CardRegistration(set = "ME3", collectorNumber = "43")
@CardRegistration(set = "IMA", collectorNumber = "65")
@CardRegistration(set = "2X2", collectorNumber = "57")
@CardRegistration(set = "OTP", collectorNumber = "11")
@CardRegistration(set = "OTP", collectorNumber = "67")
public class ManaDrain extends Card {

    public ManaDrain() {
        addSpellEffect(new RegisterDelayedManaEqualToTargetSpellManaValueEffect(
                ManaColor.COLORLESS, false, false, false));
        addSpellEffect(new CounterSpellEffect());
    }
}
