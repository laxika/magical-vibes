package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "NPH", collectorNumber = "15")
public class MarrowShards extends Card {

    public MarrowShards() {
        addSpellEffect(DealDamageEffect.toEachCreature(1, new PermanentIsAttackingPredicate()));
    }
}
