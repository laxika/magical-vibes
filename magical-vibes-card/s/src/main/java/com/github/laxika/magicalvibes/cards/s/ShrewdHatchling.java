package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "112")
@CardRegistration(set = "DDJ", collectorNumber = "8")
@CardRegistration(set = "MM2", collectorNumber = "198")
@CardRegistration(set = "MB1", collectorNumber = "198")
public class ShrewdHatchling extends Card {

    public ShrewdHatchling() {
        // This creature enters with four -1/-1 counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(4)));

        // {U/R}: Target creature can't block this creature this turn.
        addActivatedAbility(new ActivatedAbility(false, "{U/R}", List.of(new CantBlockSourceEffect(null)),
                "{U/R}: Target creature can't block this creature this turn."));

        // Whenever you cast a blue spell, remove a -1/-1 counter from this creature.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLUE),
                        List.of(new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))));

        // Whenever you cast a red spell, remove a -1/-1 counter from this creature.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED),
                        List.of(new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))));
    }
}
