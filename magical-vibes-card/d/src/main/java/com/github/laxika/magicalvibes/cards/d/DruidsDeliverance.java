package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "RTR", collectorNumber = "123")
@CardRegistration(set = "MM3", collectorNumber = "124")
@CardRegistration(set = "C19", collectorNumber = "162")
public class DruidsDeliverance extends Card {

    public DruidsDeliverance() {
        // Prevent all combat damage that would be dealt to you this turn. Populate.
        addSpellEffect(PreventDamageEffect.allToControllerFromAttackers());
        addSpellEffect(new PopulateEffect());
    }
}
