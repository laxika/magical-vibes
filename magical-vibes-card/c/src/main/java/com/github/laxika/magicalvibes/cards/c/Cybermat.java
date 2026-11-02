package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "175")
@CardRegistration(set = "WHO", collectorNumber = "780")
public class Cybermat extends Card {

    public Cybermat() {
        // Whenever this creature attacks and isn't blocked, it gets +X/+0 until end of turn,
        // where X is the number of attacking artifact creatures.
        PermanentCount attackingArtifacts = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsArtifactPredicate()
                )),
                CountScope.ANY_PLAYER);
        addTrigger(Triggers.attacksUnblocked(),
                new BoostSelfEffect(attackingArtifacts, new Fixed(0)));
    }
}
