package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "177")
public class TorporDust extends Card {

    public TorporDust() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(-3, 0, Subjects.attachedCreature()));
    }
}
