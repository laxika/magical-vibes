package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "M12", collectorNumber = "62")
public class LordOfTheUnreal extends Card {

    public LordOfTheUnreal() {
        // Illusion creatures you control get +1/+1 and have hexproof.
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ILLUSION))));
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.creaturesYouControl(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ILLUSION))));
    }
}
