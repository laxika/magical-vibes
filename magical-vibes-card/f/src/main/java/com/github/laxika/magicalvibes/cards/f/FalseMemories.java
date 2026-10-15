package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect;

@CardRegistration(set = "TOR", collectorNumber = "37")
public class FalseMemories extends Card {

    public FalseMemories() {
        addEffect(EffectSlot.SPELL, new MillEffect(7, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect(7));
    }
}
