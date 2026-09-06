package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SwiftSpiral;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "240")
public class TwiningTwins extends Card {

    public TwiningTwins() {
        setBackFaceCard(new SwiftSpiral());
        addCastingOption(new AdventureCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SwiftSpiral";
    }
}
