package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.SkipAllExtraTurnsReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SLD", collectorNumber = "1804")
@CardRegistration(set = "CMD", collectorNumber = "136")
@CardRegistration(set = "CMA", collectorNumber = "87")
public class Stranglehold extends Card {

    public Stranglehold() {
        addStaticEffect(new CantSearchLibrariesEffect(Subjects.eachOpponent(), false));
        addStaticEffect(SkipAllExtraTurnsReplacementEffect.opponents());
    }
}
