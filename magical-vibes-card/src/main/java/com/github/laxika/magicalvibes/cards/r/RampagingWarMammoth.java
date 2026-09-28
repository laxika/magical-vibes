package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "34")
@CardRegistration(set = "LTC", collectorNumber = "117")
public class RampagingWarMammoth extends Card {

    public RampagingWarMammoth() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{2}{R}",
                List.of(new DestroyEachTargetPermanentEffect(), new DrawCardEffect(1)),
                "Cycling {X}{2}{R} ({X}{2}{R}, Discard this card: Draw a card.)",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Targets must be artifacts"
                ),
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withXScaledTargets());
    }
}
