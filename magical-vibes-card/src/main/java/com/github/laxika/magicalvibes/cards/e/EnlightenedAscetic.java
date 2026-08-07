package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "12")
public class EnlightenedAscetic extends Card {

    public EnlightenedAscetic() {
        // When this creature enters, you may destroy target enchantment.
        target(TargetFilters.enchantment()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DestroyTargetPermanentEffect(),
                "Destroy target enchantment?"
        ));
    }
}
