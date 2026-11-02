package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "56")
@CardRegistration(set = "M11", collectorNumber = "57")
@CardRegistration(set = "M12", collectorNumber = "57")
public class IceCage extends Card {

    public IceCage() {
        target(TargetFilters.creature()).addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
          .addStaticEffect(new EnchantedCreatureCantActivateAbilitiesEffect())
          .addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                  new DestroyReferencedPermanentEffect(PermanentReference.SOURCE));
    }
}
