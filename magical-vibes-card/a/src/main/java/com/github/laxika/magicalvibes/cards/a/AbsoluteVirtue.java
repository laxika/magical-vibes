package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionFromOpponentsEffect;

@CardRegistration(set = "FIN", collectorNumber = "212")
@CardRegistration(set = "FIN", collectorNumber = "476")
public class AbsoluteVirtue extends Card {

    public AbsoluteVirtue() {
        addStaticEffect(new CantBeCounteredEffect());
        addStaticEffect(new PlayerHasProtectionFromOpponentsEffect());
    }
}
