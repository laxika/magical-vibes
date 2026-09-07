package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BringToLife;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "38")
public class AnimatingFaerie extends Card {

    public AnimatingFaerie() {
        setBackFaceCard(new BringToLife());
        addCastingOption(new AdventureCast("{2}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BringToLife";
    }
}
