package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "12")
public class DivineRetribution extends Card {

    public DivineRetribution() {
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureEffect(new PermanentCount(
                        new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER)));
    }
}
