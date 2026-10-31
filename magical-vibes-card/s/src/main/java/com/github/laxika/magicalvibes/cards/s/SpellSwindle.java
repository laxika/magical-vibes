package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndCreateTreasureTokensEffect;

@CardRegistration(set = "XLN", collectorNumber = "82")
@CardRegistration(set = "MOC", collectorNumber = "237")
public class SpellSwindle extends Card {

    public SpellSwindle() {
        addSpellEffect(new CounterSpellAndCreateTreasureTokensEffect());
    }
}
