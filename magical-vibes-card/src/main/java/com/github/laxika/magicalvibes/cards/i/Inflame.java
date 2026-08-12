package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;

@CardRegistration(set = "DST", collectorNumber = "64")
public class Inflame extends Card {

    public Inflame() {
        addEffect(EffectSlot.SPELL,
                new MassDamageEffect(2, false, false, new PermanentDealtDamageThisTurnPredicate()));
    }
}
