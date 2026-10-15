package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DuskSight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "TDM", collectorNumber = "80")
public class FeralDeathgorger extends Card {

    public FeralDeathgorger() {
        setBackFaceCard(new DuskSight());
        addCastingOption(new OmenCast());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(2, 0, true));
    }

    @Override
    public String getBackFaceClassName() {
        return "DuskSight";
    }
}
