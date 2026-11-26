package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "151")
public class ArtfulTakedown extends Card {

    public ArtfulTakedown() {
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        setAllowSharedTargets(true);
        addSpellEffect(ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target creature",
                        new TapPermanentsEffect(Subjects.target()),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -2/-4 until end of turn",
                        BoostEffect.target(-2, -4),
                        creatureFilter)
        )));
    }
}
