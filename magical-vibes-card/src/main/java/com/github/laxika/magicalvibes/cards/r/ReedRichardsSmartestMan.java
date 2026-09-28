package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FirstNonDrawStepDrawFourReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "MSC", collectorNumber = "636")
public class ReedRichardsSmartestMan extends Card {

    public ReedRichardsSmartestMan() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.STATIC, new FirstNonDrawStepDrawFourReplacementEffect());
    }
}
