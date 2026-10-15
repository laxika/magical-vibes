package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "CHK", collectorNumber = "154")
public class AkkiRockspeaker extends Card {

    public AkkiRockspeaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardManaEffect(ManaColor.RED, 1));
    }
}
