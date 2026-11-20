package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.s.StudentOfElements}.
 */
public class TobitaMasterOfWinds extends Card {

    public TobitaMasterOfWinds() {
        // "Creatures you control have flying." - includes Tobita itself.
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.creaturesYouControl()));
    }
}
