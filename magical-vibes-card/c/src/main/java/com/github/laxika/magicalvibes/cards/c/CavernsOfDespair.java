package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "LEG", collectorNumber = "136")
public class CavernsOfDespair extends Card {

    public CavernsOfDespair() {
        addStaticEffect(new MaximumCombatCreaturesEffect(2, 2));
    }
}
