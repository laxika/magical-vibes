package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "286")
public class BarrinsCodex extends Card {

    public BarrinsCodex() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new PutCountersOnSelfEffect(CounterType.PAGE),
                "Put a page counter on Barrin's Codex?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(new CountersOnSource(CounterType.PAGE))
                ),
                "{4}, {T}, Sacrifice Barrin's Codex: Draw X cards, where X is the number of page counters on Barrin's Codex."
        ));
    }
}
