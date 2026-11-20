package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "103")
public class GruesomeDeformity extends Card {

    public GruesomeDeformity() {
        target(TargetFilters.creature()).addStaticEffect(new GrantKeywordEffect(Keyword.INTIMIDATE, Subjects.attachedCreature()));
    }
}
