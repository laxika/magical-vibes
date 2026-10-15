package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SandAugury;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HBG", collectorNumber = "138")
public class YoungBlueDragon extends Card {

    public YoungBlueDragon() {
        setBackFaceCard(new SandAugury());
        addCastingOption(new AdventureCast("{1}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SandAugury";
    }
}
