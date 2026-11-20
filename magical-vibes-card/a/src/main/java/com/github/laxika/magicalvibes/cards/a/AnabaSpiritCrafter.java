package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HML", collectorNumber = "68")
@CardRegistration(set = "ME3", collectorNumber = "87")
public class AnabaSpiritCrafter extends Card {

    public AnabaSpiritCrafter() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.allCreatures(),
                new PermanentHasSubtypePredicate(CardSubtype.MINOTAUR)));
    }
}
