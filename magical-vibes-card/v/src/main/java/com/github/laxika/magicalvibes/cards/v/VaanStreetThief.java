package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "168")
@CardRegistration(set = "FIN", collectorNumber = "390")
@CardRegistration(set = "FIN", collectorNumber = "467")
@CardRegistration(set = "FIN", collectorNumber = "535")
public class VaanStreetThief extends Card {

    public VaanStreetThief() {
        Set<CardSubtype> thiefSubtypes = Set.of(CardSubtype.SCOUT, CardSubtype.PIRATE, CardSubtype.ROGUE);

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasAnySubtypePredicate(thiefSubtypes),
                        new ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect(),
                        false,
                        true));
        addTrigger(Triggers.youCastSpell().withFilter(new CardControllerDoesNotOwnPredicate()),
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentHasAnySubtypePredicate(thiefSubtypes)));
    }
}
