package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "66")
public class CircleOfAffliction extends Card {

    public CircleOfAffliction() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.sourceDealsDamageToYou()
                        .withFilter(new PermanentHasSourceChosenColorPredicate())
                        .withSpellFilter(new CardHasSourceChosenColorPredicate()),
                new MayPayManaEffect("{1}",
                        SequenceEffect.of(
                                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                                new GainLifeEffect(1)),
                        "Pay {1} to drain target player?"));
    }
}
