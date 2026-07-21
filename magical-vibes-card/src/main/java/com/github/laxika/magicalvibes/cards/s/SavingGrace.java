package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToEnchantedCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HOU", collectorNumber = "21")
public class SavingGrace extends Card {

    public SavingGrace() {
        // Flash and Enchant are auto-loaded keywords. Enchant creature you control.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        ))
        // When this Aura enters, all damage that would be dealt this turn to you and
        // permanents you control is dealt to enchanted creature instead.
        .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RedirectYourDamageToEnchantedCreatureThisTurnEffect())
        // Enchanted creature gets +0/+3.
        .addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 3, GrantScope.ENCHANTED_CREATURE));
    }
}
