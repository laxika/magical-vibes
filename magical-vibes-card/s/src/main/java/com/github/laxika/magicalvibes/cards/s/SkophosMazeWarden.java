package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringAbilityFromNamedLandConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "153")
public class SkophosMazeWarden extends Card {

    public SkophosMazeWarden() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, -1)),
                "{1}: This creature gets +1/-1 until end of turn."
        ));
        addTrigger(Triggers.creatureBecomesTargetOfSpellOrAbility(),
                new TriggeringAbilityFromNamedLandConditionalEffect(
                        "Labyrinth of Skophos",
                        new MayEffect(
                                new SourceFightsTargetCreatureEffect(),
                                "Have Skophos Maze-Warden fight that creature?"
                        )
                )
        );
    }
}
