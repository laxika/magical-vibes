package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "CSP", collectorNumber = "97")
public class Skred extends Card {

    public Skred() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new PermanentCount(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.CONTROLLER)));
    }
}
