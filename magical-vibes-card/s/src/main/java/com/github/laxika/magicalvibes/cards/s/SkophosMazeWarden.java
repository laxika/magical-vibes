package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

@CardRegistration(set = "THB", collectorNumber = "153")
public class SkophosMazeWarden extends Card {

    public SkophosMazeWarden() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, -1)),
                "{1}: This creature gets +1/-1 until end of turn."
        ));
        // "Whenever another creature becomes the target of an ability of a land you control named Labyrinth of
        // Skophos": the trigger's filter on the targeted creature and its source filter on the ability's permanent.
        addTrigger(Triggers.creatureBecomesTargetOfSpellOrAbility()
                        .withFilter(new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))
                        .withSource(new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNamedPredicate("Labyrinth of Skophos"),
                                new PermanentControlledBySourceControllerPredicate()))),
                new MayEffect(
                        new SourceFightsTargetCreatureEffect(),
                        "Have Skophos Maze-Warden fight that creature?"));
    }
}
