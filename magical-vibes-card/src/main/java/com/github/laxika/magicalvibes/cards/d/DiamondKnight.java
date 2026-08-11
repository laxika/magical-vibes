package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect;

@CardRegistration(set = "M20", collectorNumber = "224")
public class DiamondKnight extends Card {

    public DiamondKnight() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect(1));
    }
}
