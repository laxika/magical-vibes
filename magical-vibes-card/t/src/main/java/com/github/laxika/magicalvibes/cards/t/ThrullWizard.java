package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "46")
@CardRegistration(set = "FEM", collectorNumber = "171")
public class ThrullWizard extends Card {

    public ThrullWizard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(UnlessPaysEffect.counterUnlessPays("{3/B}")),
                "{1}{B}: Counter target black spell unless that spell's controller pays {B} or {3}.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryColorInPredicate(Set.of(CardColor.BLACK)),
                        "Target spell must be black.")
        ));
    }
}
