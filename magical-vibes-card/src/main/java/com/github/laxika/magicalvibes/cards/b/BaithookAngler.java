package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "MID", collectorNumber = "42")
public class BaithookAngler extends Card {

    public BaithookAngler() {
        setBackFaceCard(new HookHauntDrifter());

        // Disturb {1}{U}
        addCastingOption(new DisturbCast("{1}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "HookHauntDrifter";
    }
}
