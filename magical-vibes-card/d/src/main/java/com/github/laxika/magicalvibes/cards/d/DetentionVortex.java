package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "13")
public class DetentionVortex extends Card {

    public DetentionVortex() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new DestroyReferencedPermanentEffect(PermanentReference.SOURCE)),
                "Destroy this Aura. Only your opponents may activate this ability and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivatableByAnyPlayer().withActivatableOnlyByOpponents());
    }
}
