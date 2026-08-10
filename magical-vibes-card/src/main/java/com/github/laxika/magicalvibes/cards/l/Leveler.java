package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect;

@CardRegistration(set = "MRD", collectorNumber = "195")
public class Leveler extends Card {

    public Leveler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileControllerLibraryEffect());
    }
}
