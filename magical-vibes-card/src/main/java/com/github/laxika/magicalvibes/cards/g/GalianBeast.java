package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

/** Back face of {@link com.github.laxika.magicalvibes.cards.v.VincentValentine}. */
public class GalianBeast extends Card {

    public GalianBeast() {
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToBattlefieldEffect(true));
    }
}
