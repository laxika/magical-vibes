package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "111")
public class MaximizeVelocity extends Card {

    public MaximizeVelocity() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()));
        addCastingOption(new JumpStartCast());
    }
}
