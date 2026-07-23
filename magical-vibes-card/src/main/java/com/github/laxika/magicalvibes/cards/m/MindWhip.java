package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "149")
public class MindWhip extends Card {

    public MindWhip() {
        // Enchant creature. At the beginning of the upkeep of enchanted creature's controller,
        // that player may pay {3}. If they don't, this Aura deals 2 damage to that player and you
        // tap that creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        new PayManaCost("{3}"),
                        List.of(
                                new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER),
                                new TapPermanentsEffect(TapUntapScope.ENCHANTED))));
    }
}
