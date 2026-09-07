package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class ZilorthaApexOfIkoria extends Card {

    public ZilorthaApexOfIkoria() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new AssignCombatDamageAsThoughUnblockedEffect(),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))));
    }
}
