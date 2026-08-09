package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachChosenColorSpellCastEffect;

@CardRegistration(set = "M19", collectorNumber = "231")
public class DiamondMare extends Card {

    public DiamondMare() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new GainLifeForEachChosenColorSpellCastEffect());
    }
}
