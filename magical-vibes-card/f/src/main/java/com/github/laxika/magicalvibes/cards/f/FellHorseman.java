package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeathlyRide;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;

@CardRegistration(set = "WOE", collectorNumber = "92")
public class FellHorseman extends Card {

    public FellHorseman() {
        setBackFaceCard(new DeathlyRide());
        addCastingOption(new AdventureCast("{1}{B}"));
        addEffect(EffectSlot.ON_DEATH, new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DeathlyRide";
    }
}
