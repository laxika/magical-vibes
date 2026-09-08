package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RiderInNeed;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "21")
public class LonesomeUnicorn extends Card {

    public LonesomeUnicorn() {
        setBackFaceCard(new RiderInNeed());
        addCastingOption(new AdventureCast("{2}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "RiderInNeed";
    }
}
