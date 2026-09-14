package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenPlayerEffect;

@CardRegistration(set = "TSR", collectorNumber = "321")
public class TrueNameNemesis extends Card {

    public TrueNameNemesis() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChoosePlayerOnEnterEffect());
        addEffect(EffectSlot.STATIC, new ProtectionFromChosenPlayerEffect());
    }
}
