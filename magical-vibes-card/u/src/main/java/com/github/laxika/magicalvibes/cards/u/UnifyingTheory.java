package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "112")
public class UnifyingTheory extends Card {

    public UnifyingTheory() {
        // Whenever a player casts a spell, that player may pay {2}. If the player does, they draw a card.
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                UnlessPaysEffect.mayPay(Subjects.triggeringPlayer(), "{2}", new DrawCardEffect(1, Subjects.triggeringPlayer()))
                        .withPrompt("Pay {2} to draw a card?"));
    }
}
