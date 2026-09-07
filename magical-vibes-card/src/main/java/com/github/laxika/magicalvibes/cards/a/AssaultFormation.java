package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "173")
public class AssaultFormation extends Card {

    public AssaultFormation() {
        addEffect(EffectSlot.STATIC,
                new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_OWN_CREATURES));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new CanAttackAsThoughNoDefenderEffect(true)),
                "{G}: Target creature with defender can attack this turn as though it didn't have defender.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.DEFENDER))),
                        "Target must be a creature with defender")
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostAllOwnCreaturesEffect(0, 1)),
                "{2}{G}: Creatures you control get +0/+1 until end of turn."
        ));
    }
}
