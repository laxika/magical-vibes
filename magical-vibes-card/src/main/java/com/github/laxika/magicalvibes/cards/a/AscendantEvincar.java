package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostNonColorCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOtherCreaturesByColorEffect;

public class AscendantEvincar extends Card {

    public AscendantEvincar() {
        addEffect(EffectSlot.STATIC, new BoostOtherCreaturesByColorEffect(CardColor.BLACK, 1, 1));
        addEffect(EffectSlot.STATIC, new BoostNonColorCreaturesEffect(CardColor.BLACK, -1, -1));
    }
}
