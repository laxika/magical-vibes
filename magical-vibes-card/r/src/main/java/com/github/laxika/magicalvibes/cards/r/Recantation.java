package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "91")
public class Recantation extends Card {

    public Recantation() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Recantation?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), ReturnToHandEffect.target()),
                "{U}, Sacrifice this enchantment: Return up to X target permanents to their owners' hands, "
                        + "where X is the number of verse counters on this enchantment.",
                TargetFilters.permanent(),
                null,
                null,
                null,
                List.of(),
                0,
                100
        ).withSourceCounterScaledTargets(CounterType.VERSE));
    }
}
