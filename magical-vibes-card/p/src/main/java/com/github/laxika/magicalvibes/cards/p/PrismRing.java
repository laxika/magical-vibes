package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachChosenColorSpellCastEffect;

@CardRegistration(set = "ORI", collectorNumber = "235")
public class PrismRing extends Card {

    public PrismRing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new GainLifeForEachChosenColorSpellCastEffect());
    }
}
