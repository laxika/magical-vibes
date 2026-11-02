package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import com.github.laxika.magicalvibes.model.filter.StackEntryIsCopyPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "89")
public class NullProfusion extends Card {

    public NullProfusion() {
        // Skip your draw step.
        addStaticEffect(new SkipDrawStepEffect());

        // Whenever you play a card, draw a card.
        // "Play a card" = cast a spell or play a land, hence the two slots.
        // A copy of a card cast with Isochron Scepter is a spell but not a card, so it isn't "played".
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(1)),
                new StackEntryNotPredicate(new StackEntryIsCopyPredicate(true))
        ));
        addTrigger(Triggers.youPlayLand(), new DrawCardEffect(1));

        // Your maximum hand size is two.
        addStaticEffect(new SetControllerMaximumHandSizeEffect(2));
    }
}
