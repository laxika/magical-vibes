package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterOrTapSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "136")
public class RavenousVampire extends Card {

    public RavenousVampire() {
        // At the beginning of your upkeep, you may sacrifice a nonartifact creature. If you do, put a
        // +1/+1 counter on this creature. If you don't, tap this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MaySacrificePermanentForCounterOrTapSourceEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()))),
                "a nonartifact creature"));
    }
}
