package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "152")
@CardRegistration(set = "4ED", collectorNumber = "127")
public class CursedLand extends Card {

    public CursedLand() {
        // Enchant land. At the beginning of the upkeep of enchanted land's controller,
        // this Aura deals 1 damage to that player.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        )).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
