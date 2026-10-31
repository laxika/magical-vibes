package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "PLC", collectorNumber = "141")
@CardRegistration(set = "TSR", collectorNumber = "243")
public class UktabiDrake extends Card {

    public UktabiDrake() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{1}{G}{G}"));
    }
}
