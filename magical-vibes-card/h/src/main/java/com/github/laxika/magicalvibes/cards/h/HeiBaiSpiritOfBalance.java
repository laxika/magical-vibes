package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "225")
public class HeiBaiSpiritOfBalance extends Card {

    public HeiBaiSpiritOfBalance() {
        var anotherCreatureOrArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        var sacrificeTrigger = new MayEffect(
                new SacrificePermanentThenEffect(
                        anotherCreatureOrArtifact,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        "another creature or artifact"),
                "Sacrifice another creature or artifact?");

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, sacrificeTrigger);
        addEffect(EffectSlot.ON_ATTACK, sacrificeTrigger);
        target(TargetFilters.creatureYouControl()).addEffect(
                EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new PutCountersOnTargetForEachLeavingSourceCountersEffect());
    }
}
