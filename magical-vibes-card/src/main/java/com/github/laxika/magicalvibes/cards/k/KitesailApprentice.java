package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "10")
public class KitesailApprentice extends Card {

    public KitesailApprentice() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Equipped(),
                new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING), GrantScope.SELF)));
    }
}
