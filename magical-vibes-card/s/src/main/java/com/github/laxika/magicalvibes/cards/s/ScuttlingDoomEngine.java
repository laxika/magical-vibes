package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "229")
@CardRegistration(set = "DDU", collectorNumber = "61")
@CardRegistration(set = "C18", collectorNumber = "219")
public class ScuttlingDoomEngine extends Card {

    public ScuttlingDoomEngine() {
        // "This creature can't be blocked by creatures with power 2 or less."
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(2)));

        // "When this creature dies, it deals 6 damage to target opponent or planeswalker."
        // The card-level AnyTargetPredicateTargetFilter governs both sides of the death
        // trigger's target list: opponents only for players, planeswalkers only for permanents.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToTargetPlayerOrPlaneswalkerEffect(6, PlayerRelation.OPPONENT));
    }
}
