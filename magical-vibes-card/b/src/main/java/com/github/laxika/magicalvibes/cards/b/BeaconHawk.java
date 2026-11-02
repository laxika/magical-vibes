package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "3")
public class BeaconHawk extends Card {

    public BeaconHawk() {
        // Whenever this creature deals combat damage to a player, you may untap target creature.
        target(TargetFilters.creature()).addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new UntapPermanentsEffect(TapUntapScope.TARGET), "Untap target creature?"));

        // {W}: This creature gets +0/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostSelfEffect(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."
        ));
    }
}
