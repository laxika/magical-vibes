package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "127")
@CardRegistration(set = "NEM", collectorNumber = "51")
@CardRegistration(set = "DPA", collectorNumber = "19")
@CardRegistration(set = "HOP", collectorNumber = "17")
public class AscendantEvincar extends Card {

    public AscendantEvincar() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreatures(),
                new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
        addStaticEffect(new StaticBoostEffect(-1, -1, Subjects.allCreatures(),
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))));
    }
}
