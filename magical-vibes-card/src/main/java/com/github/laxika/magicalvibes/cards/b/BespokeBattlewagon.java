package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "71")
public class BespokeBattlewagon extends Card {

    public BespokeBattlewagon() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EnergyCountersEffect(2)),
                "{T}: You get {E}{E}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(2), new TapPermanentsEffect(
                        TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "{T}, Pay {E}{E}: Tap target creature.",
                TargetFilters.creature()
        ).withActivationCondition(new ControllerEnergyAtLeast(2),
                "You need at least two energy counters to activate this ability."));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(3), new DrawCardEffect(1)),
                "{T}, Pay {E}{E}{E}: Draw a card."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(4),
                        new SetCardTypesUntilEndOfTurnEffect(
                                Set.of(CardType.ARTIFACT, CardType.CREATURE), GrantScope.SELF)
                ),
                "Pay {E}{E}{E}{E}: This Vehicle becomes an artifact creature until end of turn."
        ).withActivationCondition(new ControllerEnergyAtLeast(4),
                "You need at least four energy counters to activate this ability."));
    }
}
