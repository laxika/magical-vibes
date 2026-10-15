package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "218")
public class BelligerentBrontodon extends Card {

    public BelligerentBrontodon() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageWithToughnessEffect(GrantScope.ALL_OWN_CREATURES));
    }
}
