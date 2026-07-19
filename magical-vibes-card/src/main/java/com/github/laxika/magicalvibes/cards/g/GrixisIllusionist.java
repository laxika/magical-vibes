package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "29")
public class GrixisIllusionist extends Card {

    public GrixisIllusionist() {
        // {T}: Target land you control becomes the basic land type of your choice until end of turn.
        // Type-replacing (rule 305.7): the land loses its other land types/mana ability and becomes the chosen type.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN, null, true)),
                "{T}: Target land you control becomes the basic land type of your choice until end of turn.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land you control"
                )
        ));
    }
}
