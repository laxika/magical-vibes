package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class HareRaising extends Card {

    public HareRaising() {
        PermanentCount creatureCount = new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(BoostEffect.target(creatureCount, creatureCount))
                .addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.target()));
    }
}
