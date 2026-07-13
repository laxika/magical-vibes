package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "128")
public class DarkestHour extends Card {

    public DarkestHour() {
        // All creatures are black. Layer-5 color setter (overriding) over every creature.
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLACK, GrantScope.ALL_CREATURES, true));
    }
}
