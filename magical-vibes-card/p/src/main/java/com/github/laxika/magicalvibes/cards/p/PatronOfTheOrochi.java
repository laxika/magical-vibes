package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "138")
public class PatronOfTheOrochi extends Card {

    public PatronOfTheOrochi() {
        addCastingOption(AlternateHandCast.offering(List.of(
                new ManaCastingCost("{6}{G}{G}"),
                new SacrificePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.SNAKE))
        )));
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST))),
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentColorInPredicate(Set.of(CardColor.GREEN))))))
                ),
                "{T}: Untap all Forests and all green creatures. Activate only once each turn.", 1));
    }
}
