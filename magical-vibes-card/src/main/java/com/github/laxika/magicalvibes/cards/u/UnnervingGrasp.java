package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "80")
public class UnnervingGrasp extends Card {

    public UnnervingGrasp() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        addEffect(EffectSlot.SPELL, ManifestDreadEffect.forController());
    }
}
