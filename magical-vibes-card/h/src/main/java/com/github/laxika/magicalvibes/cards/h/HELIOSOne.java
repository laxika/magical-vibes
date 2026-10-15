package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayXEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "149")
@CardRegistration(set = "PIP", collectorNumber = "441")
@CardRegistration(set = "PIP", collectorNumber = "677")
@CardRegistration(set = "PIP", collectorNumber = "969")
public class HELIOSOne extends Card {

    public HELIOSOne() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EnergyCountersEffect(1)),
                "{T}: You get {E}."
        ));

        PermanentPredicate nonlandPermanentWithManaValueX = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentManaValueEqualsXPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new PayXEnergyCost(), new SacrificeSelfCost(),
                        new DestroyTargetPermanentEffect(nonlandPermanentWithManaValueX)),
                "{3}, {T}, Pay X {E}, Sacrifice this land: Destroy target nonland permanent with mana value X.",
                new PermanentPredicateTargetFilter(nonlandPermanentWithManaValueX,
                        "Target must be a nonland permanent with mana value X."),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
