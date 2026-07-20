package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "AKH", collectorNumber = "119")
public class BlazingVolley extends Card {

    public BlazingVolley() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, false, false,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
