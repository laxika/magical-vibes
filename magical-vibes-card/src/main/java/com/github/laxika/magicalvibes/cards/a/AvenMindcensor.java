package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentSearchesTopCardsInsteadEffect;

@CardRegistration(set = "AKH", collectorNumber = "5")
public class AvenMindcensor extends Card {

    public AvenMindcensor() {
        // "If an opponent would search a library, that player searches the top four cards of that library instead."
        addEffect(EffectSlot.STATIC, new OpponentSearchesTopCardsInsteadEffect(4));
    }
}
