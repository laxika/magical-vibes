package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "DFT", collectorNumber = "103")
public class RiskyShortcut extends Card {

    public RiskyShortcut() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
    }
}
