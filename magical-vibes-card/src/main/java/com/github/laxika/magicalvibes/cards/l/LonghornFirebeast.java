package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTakeDamageSacrificeSourceEffect;

@CardRegistration(set = "TOR", collectorNumber = "103")
public class LonghornFirebeast extends Card {

    public LonghornFirebeast() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AnyOpponentMayTakeDamageSacrificeSourceEffect(5));
    }
}
