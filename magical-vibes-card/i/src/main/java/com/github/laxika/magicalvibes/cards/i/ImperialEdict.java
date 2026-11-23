package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PTK", collectorNumber = "77")
public class ImperialEdict extends Card {

    public ImperialEdict() {
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"));
        addSpellEffect(ChoosePermanentsEffect.exactly(1,
                        Subjects.controlledBy(Subjects.targetPlayerDeclaredElsewhere(), new PermanentIsCreaturePredicate()),
                        "a creature you control")
                .chosenBy(Subjects.targetPlayer()));
        addSpellEffect(new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
