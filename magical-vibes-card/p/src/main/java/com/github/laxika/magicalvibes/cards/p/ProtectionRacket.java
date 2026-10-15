package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionRacketEffect;

@CardRegistration(set = "NCC", collectorNumber = "39")
@CardRegistration(set = "NCC", collectorNumber = "140")
public class ProtectionRacket extends Card {

    public ProtectionRacket() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ProtectionRacketEffect());
    }
}
