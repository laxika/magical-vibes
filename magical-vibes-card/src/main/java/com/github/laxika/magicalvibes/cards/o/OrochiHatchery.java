package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "266")
public class OrochiHatchery extends Card {

    public OrochiHatchery() {
        // Orochi Hatchery enters the battlefield with X charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.CHARGE, new XValue()));

        // {5}, {T}: Create a 1/1 green Snake creature token for each charge counter on Orochi Hatchery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new CreateTokenEffect(
                        new CountersOnSource(CounterType.CHARGE),
                        "Snake", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SNAKE),
                        Set.of(), Set.of())),
                "{5}, {T}: Create a 1/1 green Snake creature token for each charge counter on Orochi Hatchery."
        ));
    }
}
