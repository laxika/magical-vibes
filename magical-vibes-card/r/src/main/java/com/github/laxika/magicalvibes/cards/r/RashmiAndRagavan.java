package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "8")
@CardRegistration(set = "MOC", collectorNumber = "94")
public class RashmiAndRagavan extends Card {

    public RashmiAndRagavan() {
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")).addTrigger(Triggers.youCastSpell()
                        .duringYourTurn()
                        .nthThisTurn(1),
                new ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffect());
    }
}
