package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;

@CardRegistration(set = "AFR", collectorNumber = "167")
public class Xorn extends Card {

    public Xorn() {
        addEffect(EffectSlot.STATIC, new AddTokenCreationEffect(1, CardSubtype.TREASURE));
    }
}
