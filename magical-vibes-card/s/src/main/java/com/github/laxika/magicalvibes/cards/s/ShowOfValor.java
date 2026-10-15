package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "M13", collectorNumber = "34")
@CardRegistration(set = "M14", collectorNumber = "33")
@CardRegistration(set = "M20", collectorNumber = "311")
public class ShowOfValor extends Card {

    public ShowOfValor() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 4));
    }
}
