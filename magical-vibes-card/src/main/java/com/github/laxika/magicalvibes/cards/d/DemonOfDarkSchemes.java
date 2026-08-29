package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "73")
public class DemonOfDarkSchemes extends Card {

    public DemonOfDarkSchemes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new EnergyCountersEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new PayEnergyCost(4),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build()),
                "{2}{B}, Pay {E}{E}{E}{E}: Put target creature card from a graveyard onto the battlefield under your control tapped."
        ).withActivationCondition(new ControllerEnergyAtLeast(4),
                "You need at least four energy counters to activate this ability."));
    }
}
