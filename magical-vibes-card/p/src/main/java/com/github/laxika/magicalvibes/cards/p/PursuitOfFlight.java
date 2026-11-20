package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "102")
public class PursuitOfFlight extends Card {

    public PursuitOfFlight() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{U}",
                                List.of(new GrantKeywordEffect(Keyword.FLYING, Subjects.source())),
                                "{U}: This creature gains flying until end of turn."
                        ),
                        Subjects.attachedCreature()
                ));
    }
}
