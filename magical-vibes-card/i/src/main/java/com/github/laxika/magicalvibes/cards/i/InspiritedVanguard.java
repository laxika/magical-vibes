package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "TDM", collectorNumber = "146")
public class InspiritedVanguard extends Card {

    public InspiritedVanguard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EndureEffect(2));
        addEffect(EffectSlot.ON_ATTACK, new EndureEffect(2));
    }
}
