package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "93")
public class HoardSmelterDragon extends Card {

    public HoardSmelterDragon() {
        // Destroy target artifact. Hoard-Smelter Dragon gets +X/+0, where X is that artifact's mana value.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new DestroyTargetPermanentThenEffect(
                        EventStat.MANA_VALUE, new BoostSelfEffect(new EventValue(), new Fixed(0)),
                        ThenEffectRecipient.CONTROLLER)),
                "{3}{R}: Destroy target artifact. Hoard-Smelter Dragon gets +X/+0 until end of turn, where X is that artifact's mana value.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));
    }
}
