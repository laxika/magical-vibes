package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "223")
public class MultiformWonder extends Card {

    public MultiformWonder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(1),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Flying",
                                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Vigilance",
                                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Lifelink",
                                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF))))),
                "Pay {E}: This creature gains your choice of flying, vigilance, or lifelink until end of turn."
        ).withActivationCondition(new ControllerEnergyAtLeast(1),
                "You need at least one energy counter to activate this ability."));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(1),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Gets +2/-2",
                                        new BoostSelfEffect(2, -2)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Gets -2/+2",
                                        new BoostSelfEffect(-2, 2))))),
                "Pay {E}: This creature gets +2/-2 or -2/+2 until end of turn."
        ).withActivationCondition(new ControllerEnergyAtLeast(1),
                "You need at least one energy counter to activate this ability."));
    }
}
