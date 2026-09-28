package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantTransformEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "273")
@CardRegistration(set = "MB2", collectorNumber = "509")
public class AvacynsCollarTheSymbolOfHerChurch extends Card {

    public AvacynsCollarTheSymbolOfHerChurch() {
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantTransformEffect());
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AttachSourceEquipmentToTargetCreatureEffect()),
                "Shackle {3}",
                TargetFilters.creatureAnOpponentControls(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
