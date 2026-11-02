package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "67")
public class PillarTombsOfAku extends Card {

    public PillarTombsOfAku() {
        // Triggers.upkeep(ActivePlayer.ANY) puts the active player on targetId. enchantedControllerMayPay routes
        // the may-sacrifice prompt to that player. Decline: they lose 5 life and you sacrifice this.
        // World rule is handled by SBA.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice a creature"),
                        List.of(
                                new LoseLifeEffect(5, LoseLifeRecipient.TARGET_PLAYER),
                                new SacrificeSelfEffect())));
    }
}
