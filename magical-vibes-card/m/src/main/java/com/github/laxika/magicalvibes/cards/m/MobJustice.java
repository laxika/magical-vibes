package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "STH", collectorNumber = "90")
public class MobJustice extends Card {

    public MobJustice() {
        addSpellEffect(DealDamageEffect.toTargetPlayerOrPlaneswalker(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
