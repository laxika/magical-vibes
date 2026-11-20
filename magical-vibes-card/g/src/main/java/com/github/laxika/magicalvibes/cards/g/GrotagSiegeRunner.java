package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "149")
public class GrotagSiegeRunner extends Card {

    public GrotagSiegeRunner() {
        // {R}, Sacrifice this creature: Destroy target creature with defender. This creature deals 2 damage to that creature's controller.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificeSelfCost(),
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(),
                                new DealDamageToPlayersEffect(2, Subjects.controllerOf(Subjects.target())))
                ),
                "{R}, Sacrifice Grotag Siege-Runner: Destroy target creature with defender. Grotag Siege-Runner deals 2 damage to that creature's controller.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.DEFENDER)
                        )),
                        "Target must be a creature with defender"
                )
        ));
    }
}
