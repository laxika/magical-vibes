package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "48")
public class VassalsDuty extends Card {

    public VassalsDuty() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new RedirectNextDamageEffect(RedirectRole.TARGET, RedirectRole.CONTROLLER,
                        new Fixed(1), TargetCategory.CREATURE)),
                "{1}: The next 1 damage that would be dealt to target legendary creature you control this turn is dealt to you instead.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
                        "Target must be a legendary creature you control")));
    }
}
