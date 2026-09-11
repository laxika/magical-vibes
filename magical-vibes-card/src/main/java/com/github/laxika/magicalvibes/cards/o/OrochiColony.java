package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OPC2", collectorNumber = "29")
public class OrochiColony extends Card {

    public OrochiColony() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayEffect(
                                new SearchLibraryEffect(
                                        CardPredicateUtils.basicLand(),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                                "Search your library for a basic land card?")));

        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new MakeCreatureUnblockableEffect());
    }
}
