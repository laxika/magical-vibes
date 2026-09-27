package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "42")
public class Creeperhulk extends Card {

    public Creeperhulk() {
        // {1}{G}: Until end of turn, target creature you control has base power and toughness 5/5 and gains trample.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SetBasePowerToughnessEffect(5, 5),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "{1}{G}: Until end of turn, target creature you control has base power and toughness 5/5 and gains trample.",
                TargetFilters.creatureYouControl()
        ));
    }
}
