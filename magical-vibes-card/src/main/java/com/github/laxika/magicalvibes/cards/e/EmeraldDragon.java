package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DissonantWave;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HBG", collectorNumber = "210")
public class EmeraldDragon extends Card {

    public EmeraldDragon() {
        setBackFaceCard(new DissonantWave());
        addCastingOption(new AdventureCast("{2}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DissonantWave";
    }
}
