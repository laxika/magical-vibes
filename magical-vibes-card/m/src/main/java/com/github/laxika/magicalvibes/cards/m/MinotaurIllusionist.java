package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "111")
public class MinotaurIllusionist extends Card {

    public MinotaurIllusionist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.SELF)),
                "{1}{U}: This creature gains shroud until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(new SourcePower())),
                "{R}, Sacrifice this creature: It deals damage equal to its power to target creature.",
                TargetFilters.creature()
        ));
    }
}
