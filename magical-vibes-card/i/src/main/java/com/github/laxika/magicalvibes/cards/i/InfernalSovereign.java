package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "75")
@CardRegistration(set = "MOC", collectorNumber = "83")
public class InfernalSovereign extends Card {

    public InfernalSovereign() {
        // Skip your draw step.
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());

        // Whenever you play a land or cast a spell, you draw a card and you lose 1 life.
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                SequenceEffect.of(new DrawCardEffect(), new LoseLifeEffect(1)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(), new LoseLifeEffect(1))
        ));
    }
}
