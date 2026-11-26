package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;

@CardRegistration(set = "P02", collectorNumber = "85")
@CardRegistration(set = "ME4", collectorNumber = "94")
@CardRegistration(set = "MB2", collectorNumber = "186")
public class RainOfDaggers extends Card {

    public RainOfDaggers() {
        // Destroy all creatures target opponent controls. You lose 2 life for each creature destroyed this way.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        ))
                .addSpellEffect(DestroyTargetPermanentEffect.allTargetPlayerControls(
                        new PermanentIsCreaturePredicate()))
                .addSpellEffect(new LoseLifeEffect(new Scaled(new AmountThisWay(StepKind.DESTROY), 2), Subjects.you()));
    }
}
