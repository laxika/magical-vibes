package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "64")
@CardRegistration(set = "PIP", collectorNumber = "390")
@CardRegistration(set = "PIP", collectorNumber = "592")
@CardRegistration(set = "PIP", collectorNumber = "918")
public class PlasmaCaster extends Card {

    public PlasmaCaster() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK, new EnergyCountersEffect(2));

        var blockingEquippedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentBlockingSourcePredicate()));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(2),
                        new FlipCoinWinEffect(
                                new ExileTargetPermanentEffect(),
                                new DealDamageToTargetCreatureEffect(1))
                ),
                "Pay {E}{E}: Choose target creature that's blocking equipped creature. Flip a coin. "
                        + "If you win the flip, exile the chosen creature. Otherwise, this Equipment deals 1 damage to it.",
                new PermanentPredicateTargetFilter(
                        blockingEquippedCreature,
                        "Target must be a creature blocking the equipped creature"))
                .withActivationCondition(new ControllerEnergyAtLeast(2),
                        "You need at least two energy counters to activate this ability."));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
