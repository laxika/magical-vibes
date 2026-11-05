package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "142")
public class ManticoreOfTheGauntlet extends Card {

    public ManticoreOfTheGauntlet() {
        // "When this creature enters, put a -1/-1 counter on target creature you control."
        var putCounterOnTargetPermanent = new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1);
        target(TargetFilters.creatureYouControl()).bindEffect(putCounterOnTargetPermanent);

        // "This creature deals 3 damage to target opponent or planeswalker."
        var dealDamageToTargetPlayerOrPlaneswalker = new DealDamageToTargetPlayerOrPlaneswalkerEffect(3, PlayerRelation.OPPONENT);
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker"
        )).bindEffect(dealDamageToTargetPlayerOrPlaneswalker);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                putCounterOnTargetPermanent,
                dealDamageToTargetPlayerOrPlaneswalker);
    }
}
