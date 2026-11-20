package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "184")
public class HeartwoodShard extends Card {

    public HeartwoodShard() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target())),
                "{3}, {T}: Target creature gains trample until end of turn.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target())),
                "{G}, {T}: Target creature gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
