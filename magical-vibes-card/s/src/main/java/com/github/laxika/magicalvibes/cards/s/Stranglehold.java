package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraTurnSkipReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesAtAllEffect;

@CardRegistration(set = "SLD", collectorNumber = "1804")
@CardRegistration(set = "CMD", collectorNumber = "136")
@CardRegistration(set = "CMA", collectorNumber = "87")
public class Stranglehold extends Card {

    public Stranglehold() {
        addStaticEffect(new OpponentsCantSearchLibrariesAtAllEffect());
        addStaticEffect(new OpponentExtraTurnSkipReplacementEffect());
    }
}
