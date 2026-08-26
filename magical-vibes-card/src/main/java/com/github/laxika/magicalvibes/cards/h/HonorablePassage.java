package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "VIS", collectorNumber = "7")
@CardRegistration(set = "TSB", collectorNumber = "9")
public class HonorablePassage extends Card {

    public HonorablePassage() {
        // The next time a source of your choice would deal damage to any target this turn, prevent
        // that damage. If damage from a red source is prevented this way, Honorable Passage deals
        // that much damage to the source's controller.
        addEffect(EffectSlot.SPELL,
                PreventDamageFromChosenSourceEffect.nextDamageToAnyTargetAndDamageRedSourceController());
    }
}
