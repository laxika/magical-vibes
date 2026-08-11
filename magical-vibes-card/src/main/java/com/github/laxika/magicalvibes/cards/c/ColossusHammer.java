package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "M20", collectorNumber = "223")
public class ColossusHammer extends Card {

    public ColossusHammer() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(10, 10, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new RemoveKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{8}"));
    }
}
