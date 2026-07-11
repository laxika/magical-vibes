package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "53")
public class TalasWarrior extends Card {

    public TalasWarrior() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
