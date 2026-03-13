package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedByNonColorSourcesEffect;

@CardRegistration(set = "M11", collectorNumber = "174")
public class GaeasRevenge extends Card {

    public GaeasRevenge() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.STATIC, new CantBeTargetedByNonColorSourcesEffect(CardColor.GREEN));
    }
}
