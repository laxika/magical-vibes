package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Forum's Favor, the prepare spell of Honorbound Page // Forum's Favor.
 */
public class ForumFavor extends Card {

    public ForumFavor() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.target()));
    }
}
