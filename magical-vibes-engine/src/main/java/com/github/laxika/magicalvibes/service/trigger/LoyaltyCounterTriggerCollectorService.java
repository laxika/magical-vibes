package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Collects triggers caused by loyalty counters being removed from a permanent. */
@Service
public class LoyaltyCounterTriggerCollectorService {

    /**
     * "Whenever one or more loyalty counters are removed from this, it deals that much damage to target opponent or
     * planeswalker" (Chandra, Fire Artisan): the card's effect goes on the stack as written, its target chosen as it
     * triggers, with the number of counters removed as the entry's event value ("that much").
     */
    @CollectsCounterTrigger(value = DealDamageToTargetPlayerOrPlaneswalkerEffect.class,
            watch = CounterWatch.ON_SELF_LOYALTY_COUNTERS_REMOVED)
    private boolean handleLoyaltyCountersRemoved(TriggerMatchContext match,
            DealDamageToTargetPlayerOrPlaneswalkerEffect effect, TriggerContext ctx) {
        TriggerContext.LoyaltyCountersRemoved removed = (TriggerContext.LoyaltyCountersRemoved) ctx;
        if (removed.amount() <= 0 || match.permanent() == null) {
            return false;
        }

        GameData gameData = match.gameData();
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(),
                match.controllerId(),
                new ArrayList<>(List.of(effect)),
                false,
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or planeswalker"),
                0,
                match.permanent().getId()).withEventValue(removed.amount()));
        return true;
    }
}
