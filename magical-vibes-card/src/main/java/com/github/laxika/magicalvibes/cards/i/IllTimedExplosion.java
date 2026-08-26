package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKM", collectorNumber = "207")
public class IllTimedExplosion extends Card {

    public IllTimedExplosion() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(2, DiscardRecipient.CONTROLLER),
                        new MassDamageEffect(new GreatestDiscardedCardManaValue(), false)),
                "Discard two cards?"));
    }
}
