package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;

@CardRegistration(set = "DIS", collectorNumber = "10")
public class GuardianOfTheGuildpact extends Card {

    public GuardianOfTheGuildpact() {
        addStaticEffect(ProtectionFromMulticoloredEffect.monocolored());
    }
}
