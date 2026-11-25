package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "83")
public class LiltingRefrain extends Card {

    public LiltingRefrain() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                PutCountersEffect.onSelf(CounterType.VERSE),
                "Put a verse counter on Lilting Refrain?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        UnlessPaysEffect.counterUnlessPays(new CountersOnSource(CounterType.VERSE))
                ),
                "Sacrifice Lilting Refrain: Counter target spell unless its controller pays {X}, "
                        + "where X is the number of verse counters on Lilting Refrain."
        ));
    }
}
