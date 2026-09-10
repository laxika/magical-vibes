package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RiphookRaider;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "VOW", collectorNumber = "203")
public class HookhandMariner extends Card {

    public HookhandMariner() {
        setBackFaceCard(new RiphookRaider());
    }

    @Override
    public String getBackFaceClassName() {
        return "RiphookRaider";
    }
}
