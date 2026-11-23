package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "P02", collectorNumber = "104")
public class GoblinWarCry extends Card {

    public GoblinWarCry() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));
        addSpellEffect(ChoosePermanentsEffect.exactly(1,
                        Subjects.controlledBy(Subjects.targetPlayerDeclaredElsewhere(), new PermanentIsCreaturePredicate()),
                        "a creature you control")
                .chosenBy(Subjects.targetPlayer()));
        addSpellEffect(new CantBlockThisTurnEffect(Subjects.except(
                Subjects.controlledBy(Subjects.targetPlayerDeclaredElsewhere(), new PermanentIsCreaturePredicate()),
                Subjects.thisWay(StepKind.CHOOSE))));
    }
}
