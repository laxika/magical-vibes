package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "136")
@CardRegistration(set = "5ED", collectorNumber = "167")
@CardRegistration(set = "ICE", collectorNumber = "130")
public class Hecatomb extends Card {

    public Hecatomb() {
        // When Hecatomb enters, sacrifice it unless you sacrifice four creatures.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(4, new PermanentIsCreaturePredicate()),
                List.of(new SacrificeSelfEffect()),
                true));

        // Tap an untapped Swamp you control: This enchantment deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                        new DealDamageToAnyTargetEffect(1)),
                "Tap an untapped Swamp you control: Hecatomb deals 1 damage to any target."
        ));
    }
}
