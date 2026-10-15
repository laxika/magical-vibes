package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "297")
public class ChariotOfTheSun extends Card {

    public ChariotOfTheSun() {
        // {2}, {T}: Until end of turn, target creature you control gains flying and has base
        // toughness 1. Base power is untouched, so the toughness-only setter is used.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        SetBasePowerToughnessEffect.toughnessOnly(1)
                ),
                "{2}, {T}: Until end of turn, target creature you control gains flying and has "
                        + "base toughness 1.",
                TargetFilters.creatureYouControl()
        ));
    }
}
