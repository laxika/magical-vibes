package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "161")
public class BurningPalmEfreet extends Card {

    public BurningPalmEfreet() {
        // {1}{R}{R}: This creature deals 2 damage to target creature with flying and that creature loses flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(
                        new DealDamageToTargetCreatureEffect(2),
                        new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET)
                ),
                "{1}{R}{R}: Burning Palm Efreet deals 2 damage to target creature with flying and that creature loses flying until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        "Target must be a creature with flying"
                )
        ));
    }
}
