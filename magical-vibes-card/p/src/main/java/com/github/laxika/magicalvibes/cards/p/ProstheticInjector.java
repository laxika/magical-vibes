package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "239")
public class ProstheticInjector extends Card {

    public ProstheticInjector() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.TOXIC, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(new ToxicEffect(1), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
