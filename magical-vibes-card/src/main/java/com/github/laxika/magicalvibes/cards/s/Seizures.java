package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "159")
public class Seizures extends Card {

    public Seizures() {
        // Enchant creature. Whenever enchanted creature becomes tapped, this Aura deals 3 damage
        // to that creature's controller unless that player pays {3}.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        new PayManaCost("{3}"),
                        List.of(new DealDamageToPlayersEffect(3,
                                DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER))));
    }
}
