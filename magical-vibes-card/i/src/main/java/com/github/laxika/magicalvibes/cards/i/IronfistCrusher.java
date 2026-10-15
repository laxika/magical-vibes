package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;

@CardRegistration(set = "ONS", collectorNumber = "42")
public class IronfistCrusher extends Card {

    public IronfistCrusher() {
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        addMorph("{3}{W}");
    }
}
