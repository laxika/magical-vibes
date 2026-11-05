package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "25")
public class MangarasBlessing extends Card {

    public MangarasBlessing() {
        addSpellEffect(new GainLifeEffect(5));

        // "When a spell or ability an opponent controls causes you to discard this card, you gain
        // 2 life, and you return this card from your graveyard to your hand at the beginning of
        // the next end step." The delayed return takes the discarded card, the stack entry's triggering card.
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new GainLifeEffect(2));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());
    }
}
