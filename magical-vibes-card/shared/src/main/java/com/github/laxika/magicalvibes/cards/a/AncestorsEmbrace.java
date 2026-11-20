package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class AncestorsEmbrace extends Card {

    public AncestorsEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()))
                .addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
