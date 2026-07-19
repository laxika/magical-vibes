package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "CON", collectorNumber = "58")
public class Banefire extends Card {

    public Banefire() {
        // If X is 5 or more, this spell can't be countered (checked while it is on the stack).
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect(new SpellXAtLeast(5)));

        // Banefire deals X damage to any target; if X is 5 or more, that damage can't be prevented.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue(), new SpellXAtLeast(5)));
    }
}
