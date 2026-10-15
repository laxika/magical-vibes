package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "156")
public class Stonefury extends Card {

    public Stonefury() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
