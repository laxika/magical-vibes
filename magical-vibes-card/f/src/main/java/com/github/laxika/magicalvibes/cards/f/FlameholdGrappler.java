package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;

@CardRegistration(set = "TDM", collectorNumber = "185")
public class FlameholdGrappler extends Card {

    public FlameholdGrappler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyNextSpellCastThisTurnEffect());
    }
}
