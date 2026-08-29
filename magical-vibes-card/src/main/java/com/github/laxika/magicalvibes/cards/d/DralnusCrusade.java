package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "PLS", collectorNumber = "104")
public class DralnusCrusade extends Card {

    public DralnusCrusade() {
        var goblins = new PermanentHasSubtypePredicate(CardSubtype.GOBLIN);
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES, goblins));
        addEffect(EffectSlot.STATIC,
                new GrantColorEffect(CardColor.BLACK, GrantScope.ALL_CREATURES, false, goblins));
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.ZOMBIE, GrantScope.ALL_CREATURES, false, goblins));
    }
}
