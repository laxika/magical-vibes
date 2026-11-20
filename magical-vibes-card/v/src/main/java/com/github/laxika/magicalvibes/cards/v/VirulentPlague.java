package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "DTK", collectorNumber = "125")
@CardRegistration(set = "HA2", collectorNumber = "9")
public class VirulentPlague extends Card {

    public VirulentPlague() {
        addStaticEffect(new StaticBoostEffect(-2, -2, Subjects.allCreatures(),
                new PermanentIsTokenPredicate()));
    }
}
