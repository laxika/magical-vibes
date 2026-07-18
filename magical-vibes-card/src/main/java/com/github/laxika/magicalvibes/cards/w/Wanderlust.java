package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "339")
@CardRegistration(set = "4ED", collectorNumber = "285")
public class Wanderlust extends Card {

    public Wanderlust() {
        // Enchant creature. At the beginning of the upkeep of enchanted creature's
        // controller, this Aura deals 1 damage to that player.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
