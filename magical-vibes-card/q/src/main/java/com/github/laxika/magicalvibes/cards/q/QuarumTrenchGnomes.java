package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LEG", collectorNumber = "162")
public class QuarumTrenchGnomes extends Card {

    public QuarumTrenchGnomes() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReplaceLandManaWithColorEffect(ManaColor.COLORLESS)
                        .forLands(new PermanentHasSubtypePredicate(CardSubtype.PLAINS))
                        .replacing(Set.of(ManaColor.WHITE))
                        .on(Subjects.target())
                        .lasting(Duration.PERMANENT)),
                "{T}: If target Plains is tapped for mana, it produces colorless mana instead of white mana.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.PLAINS))),
                        "Target must be a Plains")));
    }
}
