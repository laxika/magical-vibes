package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ApplyLudevicCopyEffect;

public class OlagLudevicsHubris extends Card {

    public OlagLudevicsHubris() {
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, new ApplyLudevicCopyEffect());
    }
}
