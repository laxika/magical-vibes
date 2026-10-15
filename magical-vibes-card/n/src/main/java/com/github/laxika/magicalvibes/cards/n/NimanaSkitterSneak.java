package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "116")
public class NimanaSkitterSneak extends Card {

    public NimanaSkitterSneak() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new OpponentGraveyardAtLeast(8),
                new StaticBoostEffect(1, 0, Set.of(Keyword.MENACE), GrantScope.SELF)));
    }
}
