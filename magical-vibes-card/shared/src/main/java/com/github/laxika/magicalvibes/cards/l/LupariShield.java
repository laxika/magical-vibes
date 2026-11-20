package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

public class LupariShield extends Card {

    public LupariShield() {
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.INDESTRUCTIBLE),
                Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                Duration.UNTIL_YOUR_NEXT_TURN,
                null));
    }
}
