package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "CMM", collectorNumber = "243")
public class MeteoricMace extends Card {

    public MeteoricMace() {
        // Cascade: when you cast Meteoric Mace, dig for a nonland card with lesser mana value,
        // may cast it for free, and put the remaining exiled cards on the bottom randomly.
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());

        // Equipped creature gets +4/+0 and has trample.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
