package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "61")
public class TieflingOutcasts extends Card {

    public TieflingOutcasts() {
        // Other Demons, Devils, Imps, and Tieflings you control get +1/+0.
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.otherCreaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.DEMON,
                        CardSubtype.DEVIL,
                        CardSubtype.IMP,
                        CardSubtype.TIEFLING
                ))));
    }
}
