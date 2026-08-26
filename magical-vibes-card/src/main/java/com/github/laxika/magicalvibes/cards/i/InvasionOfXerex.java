package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VertexPaladin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "242")
public class InvasionOfXerex extends Card {

    public InvasionOfXerex() {
        setBackFaceCard(new VertexPaladin());
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target());
    }

    @Override
    public String getBackFaceClassName() {
        return "VertexPaladin";
    }
}
