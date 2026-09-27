package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "251")
public class ResourcefulDefense extends Card {

    public ResourcefulDefense() {
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new PutCountersOnTargetForEachLeavingSourceCountersEffect(new PermanentTruePredicate()))
                .addEffect(EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD,
                        new PutCountersOnTargetForEachLeavingSourceCountersEffect(new PermanentTruePredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new MoveAnyNumberOfCountersFromTargetPermanentToTargetPermanentEffect()),
                "{4}{W}: Move any number of counters from target permanent you control onto a second target permanent you control.",
                List.of(TargetFilters.permanentYouControl(), TargetFilters.permanentYouControl()),
                2,
                2
        ));
    }
}
