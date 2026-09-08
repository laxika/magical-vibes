package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "322")
public class RiptideLaboratory extends Card {

    public RiptideLaboratory() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}{U}, {T}: Return target Wizard you control to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "{1}{U}, {T}: Return target Wizard you control to its owner's hand.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WIZARD),
                        "Target must be a Wizard you control"
                )
        ));
    }
}
