package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;

public class VoiceOfAll extends Card {

    public VoiceOfAll() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProtectionFromChosenColorEffect());
    }
}
