package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "129")
public class BarretWallace extends Card {

    public BarretWallace() {
        PermanentAllOfPredicate equippedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsEquippedPredicate()
        ));
        PermanentCount equippedCreatures = new PermanentCount(equippedCreature, CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ATTACK,
                new DealDamageToAttackedTargetEffect(equippedCreatures));
    }
}
