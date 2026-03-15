package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "162")
public class GraftedExoskeleton extends Card {

    public GraftedExoskeleton() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.INFECT, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new SacrificeOnUnattachEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
