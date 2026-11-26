package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

public class AbsorbEssence extends Card {

    public AbsorbEssence() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new GrantKeywordEffect(
                        Set.of(Keyword.LIFELINK, Keyword.HEXPROOF), Subjects.target()));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
