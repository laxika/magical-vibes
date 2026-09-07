package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromManaValueParityEffect;

@CardRegistration(set = "IKO", collectorNumber = "19")
public class LavabrinkVenturer extends Card {

    public LavabrinkVenturer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseManaValueParityOnEnterEffect());
        addEffect(EffectSlot.STATIC, new ProtectionFromManaValueParityEffect());
    }
}
