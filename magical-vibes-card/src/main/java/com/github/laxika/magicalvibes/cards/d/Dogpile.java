package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "RAV", collectorNumber = "120")
public class Dogpile extends Card {

    public Dogpile() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.CONTROLLER)));
    }
}
