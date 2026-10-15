package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureMustAttackTargetPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "14")
public class DulcetSirens extends Card {

    public DulcetSirens() {
        addMorph("{U}");

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new TargetCreatureMustAttackTargetPlayerThisTurnEffect()),
                "{U}, {T}: Target creature attacks target opponent this turn if able.",
                List.of(
                        TargetFilters.creature(),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")
                ),
                2,
                2
        ));
    }
}
