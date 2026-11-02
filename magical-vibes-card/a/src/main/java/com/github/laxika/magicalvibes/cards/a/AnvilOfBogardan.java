package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "141")
public class AnvilOfBogardan extends Card {

    public AnvilOfBogardan() {
        // Players have no maximum hand size.
        addStaticEffect(new PlayersHaveNoMaximumHandSizeEffect());

        // At the beginning of each player's draw step, that player draws an additional card,
        // then discards a card.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), SequenceEffect.of(
                new DrawCardForTargetPlayerEffect(1),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
