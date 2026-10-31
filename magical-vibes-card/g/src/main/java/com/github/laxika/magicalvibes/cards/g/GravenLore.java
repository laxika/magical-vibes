package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "KHM", collectorNumber = "61")
@CardRegistration(set = "KHM", collectorNumber = "344")
public class GravenLore extends Card {

    public GravenLore() {
        addSpellEffect(new ScryEffect(new SnowManaSpentToCast()));
        addSpellEffect(new DrawCardEffect(3));
    }
}
