package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "248")
public class Recycle extends Card {

    public Recycle() {
        // Skip your draw step.
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());

        // Whenever you play a card, draw a card.
        // "Play a card" = cast a spell or play a land, hence the two slots.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect(1))
        ));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, new DrawCardEffect(1));

        // Your maximum hand size is two.
        addEffect(EffectSlot.STATIC, new SetControllerMaximumHandSizeEffect(2));
    }
}
