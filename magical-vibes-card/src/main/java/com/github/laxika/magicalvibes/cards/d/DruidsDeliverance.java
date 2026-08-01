package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "RTR", collectorNumber = "123")
public class DruidsDeliverance extends Card {

    public DruidsDeliverance() {
        // Prevent all combat damage that would be dealt to you this turn. Populate.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToControllerFromAttackers());
        addEffect(EffectSlot.SPELL, new PopulateEffect());
    }
}
