package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZEN", collectorNumber = "61")
public class RiteOfReplication extends Card {

    public RiteOfReplication() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{5}"));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new CreateTokenCopyOfTargetPermanentEffect(),
                SequenceEffect.of(
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new CreateTokenCopyOfTargetPermanentEffect(),
                        new CreateTokenCopyOfTargetPermanentEffect()
                )
        ));
    }
}
