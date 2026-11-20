package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNoAbilitiesPredicate;

@CardRegistration(set = "FUT", collectorNumber = "146")
@CardRegistration(set = "TSR", collectorNumber = "220")
@CardRegistration(set = "YDFT", collectorNumber = "31")
public class MuragandaPetroglyphs extends Card {

    public MuragandaPetroglyphs() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.otherCreatures(),
                new PermanentHasNoAbilitiesPredicate()));
    }
}
