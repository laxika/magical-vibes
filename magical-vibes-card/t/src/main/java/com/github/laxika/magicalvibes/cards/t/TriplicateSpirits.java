package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M15", collectorNumber = "40")
@CardRegistration(set = "PIO", collectorNumber = "41")
public class TriplicateSpirits extends Card {

    public TriplicateSpirits() {
        addSpellEffect(CreateTokenEffect.whiteSpirit(3));
    }
}
