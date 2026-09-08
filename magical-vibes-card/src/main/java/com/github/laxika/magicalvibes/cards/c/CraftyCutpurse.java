package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CraftyCutpurseEffect;

@CardRegistration(set = "RIX", collectorNumber = "33")
public class CraftyCutpurse extends Card {

    public CraftyCutpurse() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CraftyCutpurseEffect());
    }
}
