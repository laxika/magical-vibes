package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "112")
public class WizardMentor extends Card {

    public WizardMentor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ReturnToHandEffect.self(), ReturnToHandEffect.target()),
                "{T}: Return Wizard Mentor and target creature you control to their owners' hands.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control")));
    }
}
