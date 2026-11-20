package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "59")
public class AboshansDesire extends Card {

    public AboshansDesire() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()))
                .addStaticEffect(new ConditionalEffect(
                        new GraveyardCardThreshold(7, null),
                        new GrantKeywordEffect(Keyword.SHROUD, Subjects.attachedCreature())));
    }
}
