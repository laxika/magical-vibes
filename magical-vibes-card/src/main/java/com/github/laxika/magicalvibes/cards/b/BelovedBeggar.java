package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GenerousSoul;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "MID", collectorNumber = "3")
public class BelovedBeggar extends Card {

    public BelovedBeggar() {
        setBackFaceCard(new GenerousSoul());

        // Disturb {4}{W}{W}
        addCastingOption(new DisturbCast("{4}{W}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GenerousSoul";
    }
}
