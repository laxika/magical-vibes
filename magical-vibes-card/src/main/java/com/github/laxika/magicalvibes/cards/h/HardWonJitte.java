package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TMT", collectorNumber = "91")
public class HardWonJitte extends Card {

    public HardWonJitte() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
