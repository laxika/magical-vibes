package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;

@CardRegistration(set = "SLD", collectorNumber = "2198")
public class JoelResoluteSurvivor extends Card {

    public JoelResoluteSurvivor() {
        // Whenever a creature token dies, put a +1/+1 counter on Joel and draw a card.
        // This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OncePerTurnTriggerEffect(new TriggeringCardConditionalEffect(
                        new CardIsTokenPredicate(),
                        SequenceEffect.of(
                                new PutCountersOnSourceEffect(1, 1, 1),
                                new DrawCardEffect(1)))));
    }
}
