package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

/** Teferi's Tutelage — enchantment. */
@CardRegistration(set = "M21", collectorNumber = "78")
public class TeferisTutelage extends Card {

    public TeferisTutelage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new MillEffect(2, MillRecipient.EACH_OPPONENT));
    }
}
