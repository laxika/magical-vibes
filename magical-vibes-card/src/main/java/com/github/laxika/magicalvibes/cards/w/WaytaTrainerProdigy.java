package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.amount.FixedIfAllTargetsControlledByController;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "7")
@CardRegistration(set = "LCC", collectorNumber = "32")
public class WaytaTrainerProdigy extends Card {

    public WaytaTrainerProdigy() {
        addEffect(EffectSlot.STATIC,
                AdditionalTriggeredAbilityEffect.forControlledCreatureBeingDealtDamage());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new ReduceActivationCostEffect(
                                new FixedIfAllTargetsControlledByController(2, 0)),
                        new FightTargetsEffect()),
                "{2}{G}, {T}: Target creature you control fights another target creature. This ability costs {2} less to activate if it targets two creatures you control.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creature()),
                2,
                2));
    }
}
