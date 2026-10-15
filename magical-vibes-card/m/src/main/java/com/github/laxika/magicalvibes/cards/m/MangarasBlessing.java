package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;

@CardRegistration(set = "MIR", collectorNumber = "25")
public class MangarasBlessing extends Card {

    public MangarasBlessing() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(5));

        // "When a spell or ability an opponent controls causes you to discard this card, you gain
        // 2 life, and you return this card from your graveyard to your hand at the beginning of
        // the next end step." The null cardId makes the delayed return use the discarded card.
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
    }
}
