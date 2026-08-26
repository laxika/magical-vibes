package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionFromOpponentsEffect;

@CardRegistration(set = "FIN", collectorNumber = "212")
public class AbsoluteVirtue extends Card {

    public AbsoluteVirtue() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new PlayerHasProtectionFromOpponentsEffect());
    }
}
