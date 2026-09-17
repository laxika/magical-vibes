package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "56")
@CardRegistration(set = "PLS", collectorNumber = "19")
@CardRegistration(set = "DMR", collectorNumber = "34")
@CardRegistration(set = "CMD", collectorNumber = "35")
public class VoiceOfAll extends Card {

    public VoiceOfAll() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProtectionFromChosenColorEffect());
    }
}
