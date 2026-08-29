package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreaturesDealPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "164")
public class AlliesAtLast extends Card {

    public AlliesAtLast() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ALLY), CountScope.CONTROLLER)));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new TargetCreaturesDealPowerDamageToTargetEffect(1, 0));
        target(TargetFilters.creatureYouControl(), 0, 2);
    }
}
