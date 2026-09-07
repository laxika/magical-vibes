package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToDyingPowerEffect;

import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "111")
public class MaskOfGriselbrand extends Card {

    public MaskOfGriselbrand() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new MayPayLifeAndDrawEqualToDyingPowerEffect());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
