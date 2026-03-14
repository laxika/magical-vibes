package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "23")
@CardRegistration(set = "M11", collectorNumber = "24")
public class PalaceGuard extends Card {

    public PalaceGuard() {
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
    }
}
