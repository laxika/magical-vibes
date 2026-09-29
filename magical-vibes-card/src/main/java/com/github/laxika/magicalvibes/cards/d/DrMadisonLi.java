package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "3")
@CardRegistration(set = "PIP", collectorNumber = "341")
@CardRegistration(set = "PIP", collectorNumber = "531")
@CardRegistration(set = "PIP", collectorNumber = "869")
@CardRegistration(set = "PIP", collectorNumber = "1066")
public class DrMadisonLi extends Card {

    public DrMadisonLi() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ARTIFACT),
                List.of(new EnergyCountersEffect(1))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayEnergyCost(1),
                        new BoostTargetCreatureEffect(1, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)
                ),
                "{T}, Pay {E}: Target creature gets +1/+0 and gains trample and haste until end of turn.",
                TargetFilters.creature()
        ).withActivationCondition(new ControllerEnergyAtLeast(1),
                "You need at least one energy counter to activate this ability."));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayEnergyCost(3), new DrawCardEffect()),
                "{T}, Pay {E}{E}{E}: Draw a card."
        ).withActivationCondition(new ControllerEnergyAtLeast(3),
                "You need at least three energy counters to activate this ability."));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayEnergyCost(5),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.ARTIFACT))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build()
                ),
                "{T}, Pay {E}{E}{E}{E}{E}: Return target artifact card from your graveyard to the battlefield tapped."
        ).withActivationCondition(new ControllerEnergyAtLeast(5),
                "You need at least five energy counters to activate this ability."));
    }
}
