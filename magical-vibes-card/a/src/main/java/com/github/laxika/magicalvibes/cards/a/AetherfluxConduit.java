package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.CastAnyNumberOfSpellsFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "DRC", collectorNumber = "17")
@CardRegistration(set = "DRC", collectorNumber = "33")
public class AetherfluxConduit extends Card {

    public AetherfluxConduit() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new EnergyCountersEffect(new XValue()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayEnergyCost(50),
                        new DrawCardEffect(7),
                        new CastAnyNumberOfSpellsFromHandWithoutPayingManaCostEffect()
                ),
                "{T}, Pay fifty {E}: Draw seven cards. You may cast any number of spells from your hand without paying their mana costs."
        ).withActivationCondition(new ControllerEnergyAtLeast(50),
                "You need at least fifty energy counters to activate this ability."));
    }
}
