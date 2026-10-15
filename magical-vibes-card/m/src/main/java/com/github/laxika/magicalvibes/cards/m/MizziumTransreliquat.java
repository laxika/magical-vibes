package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetArtifactUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "153")
public class MizziumTransreliquat extends Card {

    public MizziumTransreliquat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new BecomeCopyOfTargetArtifactUntilEndOfTurnEffect(false)),
                "{3}: This artifact becomes a copy of target artifact until end of turn.",
                TargetFilters.artifact()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{R}",
                List.of(new BecomeCopyOfTargetArtifactUntilEndOfTurnEffect(true)),
                "{1}{U}{R}: This artifact becomes a copy of target artifact, except it has this ability.",
                TargetFilters.artifact()
        ));
    }
}
