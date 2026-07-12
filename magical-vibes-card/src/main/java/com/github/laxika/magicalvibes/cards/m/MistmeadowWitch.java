package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "144")
public class MistmeadowWitch extends Card {

    public MistmeadowWitch() {
        // {2}{W}{U}: Exile target creature. Return that card to the battlefield under its
        // owner's control at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{U}",
                List.of(FlickerEffect.exileTargetReturnAtEndStep()),
                "{2}{W}{U}: Exile target creature. Return that card to the battlefield under its owner's control at the beginning of the next end step.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")
        ));
    }
}
