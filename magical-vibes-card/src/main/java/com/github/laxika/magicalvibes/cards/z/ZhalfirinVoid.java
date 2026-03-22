package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "DOM", collectorNumber = "249")
public class ZhalfirinVoid extends Card {

    public ZhalfirinVoid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));
    }
}
