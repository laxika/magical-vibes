package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RipTheSeams;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "239")
public class ThreadbindCliqueRipTheSeams extends Card {

    public ThreadbindCliqueRipTheSeams() {
        setBackFaceCard(new RipTheSeams());
        addCastingOption(new AdventureCast("{2}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "RipTheSeams";
    }
}
