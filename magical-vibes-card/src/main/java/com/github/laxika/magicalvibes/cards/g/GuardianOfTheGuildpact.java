package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMonocoloredEffect;

@CardRegistration(set = "DIS", collectorNumber = "10")
public class GuardianOfTheGuildpact extends Card {

    public GuardianOfTheGuildpact() {
        addEffect(EffectSlot.STATIC, new ProtectionFromMonocoloredEffect());
    }
}
