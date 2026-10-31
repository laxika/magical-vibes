package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "DST", collectorNumber = "29")
public class PulseOfTheGrid extends Card {

    public PulseOfTheGrid() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addSpellEffect(new ConditionalEffect(
                new AnOpponentHasMoreCardsInHandThanController(), ReturnToHandEffect.selfSpell()));
    }
}
