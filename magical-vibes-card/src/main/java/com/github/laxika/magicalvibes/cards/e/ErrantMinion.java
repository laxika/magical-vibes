package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerMayPayToPreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ICE", collectorNumber = "68")
public class ErrantMinion extends Card {

    public ErrantMinion() {
        // Enchant creature. At the beginning of the upkeep of enchanted creature's controller,
        // that player may pay any amount of mana. This Aura deals 2 damage to that player. Prevent X
        // of that damage, where X is the amount of mana that player paid this way.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new EnchantedControllerMayPayToPreventDamageEffect(2));
    }
}
