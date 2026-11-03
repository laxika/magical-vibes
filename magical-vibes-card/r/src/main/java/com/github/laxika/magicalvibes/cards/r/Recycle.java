package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "248")
@CardRegistration(set = "TPR", collectorNumber = "189")
public class Recycle extends Card {

    public Recycle() {
        // Skip your draw step.
        addStaticEffect(new SkipDrawStepEffect());

        // Whenever you play a card, draw a card.
        // "Play a card" = cast a spell or play a land, hence the two slots.
        addTrigger(Triggers.youCastSpell().anySpell(), new DrawCardEffect(1));
        addTrigger(Triggers.youPlayLand(), new DrawCardEffect(1));

        // Your maximum hand size is two.
        addStaticEffect(new SetControllerMaximumHandSizeEffect(2));
    }
}
