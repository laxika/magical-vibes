package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "115")
public class DragonAppeasement extends Card {

    public DragonAppeasement() {
        // Skip your draw step.
        addStaticEffect(new SkipDrawStepEffect());
        // Whenever you sacrifice a creature, you may draw a card.
        addTrigger(Triggers.sacrificed(new PermanentIsCreaturePredicate(), ObjectRelation.YOU_CONTROL),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
