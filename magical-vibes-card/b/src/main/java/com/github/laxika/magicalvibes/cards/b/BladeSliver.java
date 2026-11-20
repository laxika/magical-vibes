package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "88")
public class BladeSliver extends Card {

    public BladeSliver() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.allCreatures(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
