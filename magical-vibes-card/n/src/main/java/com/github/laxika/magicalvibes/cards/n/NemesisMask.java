package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "DST", collectorNumber = "134")
public class NemesisMask extends Card {

    public NemesisMask() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
