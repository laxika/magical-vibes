package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "139")
public class HearthbornBattler extends Card {

    public HearthbornBattler() {
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")).addTrigger(Triggers.playerCastsSpell()
                        .nthThisTurn(2),
                DealDamageEffect.to(Subjects.targetPlayer(), 2));
    }
}
