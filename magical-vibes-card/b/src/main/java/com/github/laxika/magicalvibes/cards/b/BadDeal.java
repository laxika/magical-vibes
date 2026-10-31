package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "M21", collectorNumber = "89")
@CardRegistration(set = "ANB", collectorNumber = "45")
public class BadDeal extends Card {

    public BadDeal() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.EACH_OPPONENT));
        addSpellEffect(new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
    }
}
