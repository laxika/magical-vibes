package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "VIS", collectorNumber = "141")
public class AnvilOfBogardan extends Card {

    public AnvilOfBogardan() {
        // Players have no maximum hand size.
        addEffect(EffectSlot.STATIC, new PlayersHaveNoMaximumHandSizeEffect());

        // At the beginning of each player's draw step, that player draws an additional card,
        // then discards a card.
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, SequenceEffect.of(
                new DrawCardForTargetPlayerEffect(1),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
