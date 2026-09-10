package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "150")
public class Outnumber extends Card {

    public Outnumber() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
