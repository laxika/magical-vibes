package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "72")
public class AntiMagicAura extends Card {

    public AntiMagicAura() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
        // Enchanted creature can't be the target of spells (abilities can still target it).
        .addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(TargetingRestrictionEffect.spells(), GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature can't be enchanted by other Auras.
        .addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(new CantBeEnchantedByOtherAurasEffect(), GrantScope.ENCHANTED_CREATURE));
    }
}
