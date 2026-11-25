package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "150")
public class Outnumber extends Card {

    public Outnumber() {
        addSpellEffect(DealDamageEffect.toTargetCreature(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
