package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetSelfPowerToughnessFromTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "19")
public class SwornDefender extends Card {

    public SwornDefender() {
        var inCombatWithThis = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentInCombatWithSourcePredicate()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SetSelfPowerToughnessFromTargetCreatureEffect(-1, 1, inCombatWithThis)),
                "{1}: This creature's power becomes the toughness of target creature blocking or being blocked by this creature minus 1 until end of turn, and its toughness becomes 1 plus the power of that creature until end of turn.",
                new PermanentPredicateTargetFilter(
                        inCombatWithThis,
                        "Target must be a creature blocking or blocked by this creature"
                )
        ));
    }
}
