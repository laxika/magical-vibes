package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EntryDenied;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "43")
public class BelunasGatekeeper extends Card {

    public BelunasGatekeeper() {
        setBackFaceCard(new EntryDenied());
        addCastingOption(new AdventureCast("{1}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "EntryDenied";
    }
}
