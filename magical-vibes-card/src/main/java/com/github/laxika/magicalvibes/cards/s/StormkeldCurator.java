package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GiantSecrets;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;

@CardRegistration(set = "YWOE", collectorNumber = "26")
public class StormkeldCurator extends Card {

    public StormkeldCurator() {
        setBackFaceCard(new GiantSecrets());
        addCastingOption(new AdventureCast("{X}{U}{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new AttachAurasToSourceEffect(false, false, Integer.MAX_VALUE, false));
    }

    @Override
    public String getBackFaceClassName() {
        return "GiantSecrets";
    }
}
