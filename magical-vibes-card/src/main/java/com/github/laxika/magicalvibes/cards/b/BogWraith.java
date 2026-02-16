package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LandwalkEffect;

public class BogWraith extends Card {

    public BogWraith() {
        addEffect(EffectSlot.STATIC, new LandwalkEffect(CardSubtype.SWAMP));
    }
}
