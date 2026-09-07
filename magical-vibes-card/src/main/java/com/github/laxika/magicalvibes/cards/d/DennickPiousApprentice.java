package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsCantBeTargetedEffect;

@CardRegistration(set = "MID", collectorNumber = "217")
public class DennickPiousApprentice extends Card {

    public DennickPiousApprentice() {
        setBackFaceCard(new DennickPiousApparition());

        // Cards in graveyards can't be the targets of spells or abilities.
        addEffect(EffectSlot.STATIC, new GraveyardCardsCantBeTargetedEffect());

        // Disturb {2}{W}{U}
        addCastingOption(new DisturbCast("{2}{W}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DennickPiousApparition";
    }
}
