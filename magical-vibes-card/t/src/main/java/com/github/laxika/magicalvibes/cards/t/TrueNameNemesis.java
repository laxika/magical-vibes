package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenPlayerEffect;

@CardRegistration(set = "TSR", collectorNumber = "321")
@CardRegistration(set = "C13", collectorNumber = "63")
public class TrueNameNemesis extends Card {

    public TrueNameNemesis() {
        addStaticEffect(new ChoosePlayerOnEnterEffect());
        addStaticEffect(new ProtectionFromChosenPlayerEffect());
    }
}
