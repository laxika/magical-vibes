package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;

@CardRegistration(set = "TSP", collectorNumber = "256")
public class Hivestone extends Card {

    public Hivestone() {
        addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(CardSubtype.SLIVER, GrantScope.OWN_CREATURES));
    }
}
