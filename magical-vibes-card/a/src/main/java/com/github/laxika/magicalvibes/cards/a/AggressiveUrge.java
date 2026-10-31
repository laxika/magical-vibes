package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "10E", collectorNumber = "250")
@CardRegistration(set = "INV", collectorNumber = "181")
@CardRegistration(set = "RIX", collectorNumber = "122")
public class AggressiveUrge extends Card {

    public AggressiveUrge() {
        addSpellEffect(new BoostTargetCreatureEffect(1, 1));
        addSpellEffect(new DrawCardEffect());
    }
}
