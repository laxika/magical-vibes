package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "39")
public class UnwantedRemake extends Card {

    public UnwantedRemake() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                ManifestDreadEffect.forController(), ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
