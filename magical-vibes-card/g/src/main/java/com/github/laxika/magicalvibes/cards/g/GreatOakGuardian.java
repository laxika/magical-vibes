package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "37")
@CardRegistration(set = "LTC", collectorNumber = "247")
@CardRegistration(set = "C19", collectorNumber = "170")
@CardRegistration(set = "SCD", collectorNumber = "186")
@CardRegistration(set = "CMA", collectorNumber = "113")
public class GreatOakGuardian extends Card {

    public GreatOakGuardian() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        BoostEffect.creaturesTargetPlayerControls(2, 2),
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.targetPlayerDeclaredElsewhere(),
                                new PermanentIsCreaturePredicate())));
    }
}
