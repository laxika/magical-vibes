package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "5")
public class AysenHighway extends Card {

    public AysenHighway() {
        // White creatures have plainswalk.
        addStaticEffect(new GrantKeywordEffect(Keyword.PLAINSWALK, Subjects.allCreatures(),
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
