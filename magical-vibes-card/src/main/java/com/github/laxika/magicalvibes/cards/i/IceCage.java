package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M10", collectorNumber = "56")
@CardRegistration(set = "M11", collectorNumber = "57")
@CardRegistration(set = "M12", collectorNumber = "57")
public class IceCage extends Card {

    public IceCage() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect())
          .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect())
          .addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                  new DestroyReferencedPermanentEffect(PermanentReference.SOURCE));
    }
}
