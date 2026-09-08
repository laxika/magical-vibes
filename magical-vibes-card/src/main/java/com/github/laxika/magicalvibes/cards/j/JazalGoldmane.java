package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "497")
public class JazalGoldmane extends Card {

    public JazalGoldmane() {
        // {3}{W}{W}: Attacking creatures you control get +X/+X until end of turn, where X is the
        // number of attacking creatures.
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();
        PermanentCount attackingCreatures = new PermanentCount(attacking, CountScope.ANY_PLAYER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(new BoostAllOwnCreaturesEffect(attackingCreatures, attackingCreatures, attacking)),
                "{3}{W}{W}: Attacking creatures you control get +X/+X until end of turn, where X is the number of attacking creatures."
        ));
    }
}
