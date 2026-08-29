package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "36")
public class DimirInformant extends Card {

    public DimirInformant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(2));
    }
}
