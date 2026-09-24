package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOC", collectorNumber = "200")
public class ReplicationTechnique extends Card {

    public ReplicationTechnique() {
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect());
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new DemonstrateEffect(), "Copy Replication Technique?"));
    }
}
