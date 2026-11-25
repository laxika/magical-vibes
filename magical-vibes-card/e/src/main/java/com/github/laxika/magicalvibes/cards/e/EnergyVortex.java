package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "64")
public class EnergyVortex extends Card {

    public EnergyVortex() {
        // "As this enchantment enters, choose an opponent." The chosen player is recorded on entry and
        // keeps being the chosen player if control of the enchantment changes.
        addStaticEffect(new ChooseOpponentOnEnterEffect());

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RemoveAllCountersEffect(CounterType.VORTEX));

        // At the beginning of the chosen player's upkeep, deal 3 damage to them unless they pay {1}
        // for each vortex counter.
        addTrigger(Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER),
                MayPayManaEffect.dynamic(new CountersOnSource(CounterType.VORTEX), null,
                        "Pay {1} for each vortex counter? If you don't, it deals 3 damage to you.",
                        MayPayPayer.TRIGGERING_PLAYER,
                        new DealDamageToPlayersEffect(3, Subjects.chosenPlayer())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(PutCountersEffect.onSelf(CounterType.VORTEX, new XValue())),
                "{X}: Put X vortex counters on Energy Vortex. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
