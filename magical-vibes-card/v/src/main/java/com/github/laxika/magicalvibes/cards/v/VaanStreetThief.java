package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "168")
@CardRegistration(set = "FIN", collectorNumber = "390")
@CardRegistration(set = "FIN", collectorNumber = "467")
@CardRegistration(set = "FIN", collectorNumber = "535")
public class VaanStreetThief extends Card {

    public VaanStreetThief() {
        Set<CardSubtype> thiefSubtypes = Set.of(CardSubtype.SCOUT, CardSubtype.PIRATE, CardSubtype.ROGUE);

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasAnySubtypePredicate(thiefSubtypes),
                        new ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect(),
                        false,
                        true));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardControllerDoesNotOwnPredicate(),
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentHasAnySubtypePredicate(thiefSubtypes)))));
    }
}
