package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "84")
public class GreaterStoneSpirit extends Card {

    public GreaterStoneSpirit() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(
                        new BoostTargetCreatureEffect(0, 2),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        false,
                                        "{R}",
                                        List.of(new BoostSelfEffect(1, 0)),
                                        "{R}: This creature gets +1/+0 until end of turn."
                                ),
                                GrantScope.TARGET,
                                null,
                                EffectDuration.UNTIL_END_OF_TURN
                        )
                ),
                "{2}{R}: Until end of turn, target creature gets +0/+2 and gains \"{R}: This creature gets +1/+0 until end of turn.\"",
                TargetFilters.creature()
        ));
    }
}
