package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardFromGraveyardOnEnterEffect;

/** Back face of Invasion of Amonkhet. */
public class LazotepConvert extends Card {

    public LazotepConvert() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyCreatureCardFromGraveyardOnEnterEffect());
    }
}
