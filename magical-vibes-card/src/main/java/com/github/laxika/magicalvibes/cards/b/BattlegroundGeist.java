package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "45")
public class BattlegroundGeist extends Card {

    public BattlegroundGeist() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SPIRIT))));
    }
}
