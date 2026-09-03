package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "139")
public class CapennaExpress extends Card {

    public CapennaExpress() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.TREASURE), "a Treasure"),
                        AnimatePermanentsEffect.crew()
                ),
                "Sacrifice a Treasure: This Vehicle becomes an artifact creature until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
