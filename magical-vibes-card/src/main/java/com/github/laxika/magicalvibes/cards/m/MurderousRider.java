package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SwiftEnd;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;

@CardRegistration(set = "ELD", collectorNumber = "97")
public class MurderousRider extends Card {

    public MurderousRider() {
        setBackFaceCard(new SwiftEnd());
        addCastingOption(new AdventureCast("{1}{B}{B}"));
        addEffect(EffectSlot.ON_DEATH, new PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "SwiftEnd";
    }
}
