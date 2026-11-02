package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "75")
@CardRegistration(set = "MOC", collectorNumber = "83")
public class InfernalSovereign extends Card {

    public InfernalSovereign() {
        // Skip your draw step.
        addStaticEffect(new SkipDrawStepEffect());

        // Whenever you play a land or cast a spell, you draw a card and you lose 1 life.
        addTrigger(Triggers.youPlayLand(),
                SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1)));
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(), new LoseLifeEffect(1))
        ));
    }
}
