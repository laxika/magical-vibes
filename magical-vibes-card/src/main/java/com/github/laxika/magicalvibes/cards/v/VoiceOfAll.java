package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "56")
public class VoiceOfAll extends Card {

    public VoiceOfAll() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProtectionFromChosenColorEffect());
    }
}
