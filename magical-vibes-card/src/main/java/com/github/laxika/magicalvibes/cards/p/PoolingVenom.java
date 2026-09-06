package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "74")
public class PoolingVenom extends Card {

    public PoolingVenom() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED)),
                "{3}{B}: Destroy enchanted land."
        ));
    }
}
