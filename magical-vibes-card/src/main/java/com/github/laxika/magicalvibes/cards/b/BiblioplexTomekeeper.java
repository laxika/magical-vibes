package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreatureUnpreparedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "247")
public class BiblioplexTomekeeper extends Card {

    public BiblioplexTomekeeper() {
        PermanentPredicateTargetFilter creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        );

        // When this creature enters, choose up to one —
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature becomes prepared",
                        new MakeTargetCreaturePreparedEffect(),
                        creatureFilter
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature becomes unprepared",
                        new MakeTargetCreatureUnpreparedEffect(),
                        creatureFilter
                )
        ), true));
    }
}
