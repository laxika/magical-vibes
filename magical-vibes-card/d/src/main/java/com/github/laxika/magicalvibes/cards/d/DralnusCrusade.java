package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "PLS", collectorNumber = "104")
@CardRegistration(set = "DMR", collectorNumber = "189")
public class DralnusCrusade extends Card {

    public DralnusCrusade() {
        var goblins = new PermanentHasSubtypePredicate(CardSubtype.GOBLIN);
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES, goblins));
        addStaticEffect(new GrantColorEffect(CardColor.BLACK, GrantScope.ALL_PERMANENTS, true, goblins));
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.ZOMBIE, GrantScope.ALL_PERMANENTS, false, goblins));
    }
}
