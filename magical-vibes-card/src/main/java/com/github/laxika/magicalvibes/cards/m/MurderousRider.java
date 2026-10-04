package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SwiftEnd;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;

@CardRegistration(set = "ELD", collectorNumber = "97")
@CardRegistration(set = "ELD", collectorNumber = "287")
@CardRegistration(set = "SLD", collectorNumber = "1981")
@CardRegistration(set = "FIC", collectorNumber = "279")
@CardRegistration(set = "MOC", collectorNumber = "258")
@CardRegistration(set = "DRC", collectorNumber = "45")
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
