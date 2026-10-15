package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DiviningDive;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekChosenLandOrNonlandEffect;

@CardRegistration(set = "YTDM", collectorNumber = "4")
public class CunningAzurescaleDiviningDive extends Card {

    public CunningAzurescaleDiviningDive() {
        setBackFaceCard(new DiviningDive());
        addCastingOption(new AdventureCast("{1}{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SeekChosenLandOrNonlandEffect(2));
    }

    @Override
    public String getBackFaceClassName() {
        return "DiviningDive";
    }
}
