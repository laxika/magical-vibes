package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M11", collectorNumber = "89")
@CardRegistration(set = "SHM", collectorNumber = "62")
@CardRegistration(set = "7ED", collectorNumber = "124")
public class Corrupt extends Card {

    public Corrupt() {
        // Deals damage to any target equal to the number of Swamps you control...
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER)));

        // ...and you gain life equal to that Swamp count. (Oracle text reads "life equal to the
        // damage dealt this way", but the engine does not thread prevented damage through to the
        // life amount, so this reproduces the pre-fold behavior of gaining the full count.)
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER)));
    }
}
