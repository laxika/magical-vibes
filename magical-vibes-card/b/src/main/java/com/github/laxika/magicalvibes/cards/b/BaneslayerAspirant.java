package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEmblem;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "3")
public class BaneslayerAspirant extends Card {

    public BaneslayerAspirant() {
        // Baneslayer Aspirant gets +3/+3 and has flying, first strike, and lifelink as long as you
        // have one or more emblems.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasEmblem(),
                new StaticBoostEffect(3, 3, Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK),
                        GrantScope.SELF)));
    }
}
