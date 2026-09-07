package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "257")
public class ThinkingCap extends Card {

    public ThinkingCap() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility(
                "{1}",
                new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE),
                "Target must be a Detective creature you control"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
