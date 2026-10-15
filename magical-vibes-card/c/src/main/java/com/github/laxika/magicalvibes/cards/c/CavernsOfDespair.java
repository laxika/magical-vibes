package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "LEG", collectorNumber = "136")
public class CavernsOfDespair extends Card {

    public CavernsOfDespair() {
        addEffect(EffectSlot.STATIC, new MaximumCombatCreaturesEffect(2, 2));
    }
}
