package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "101")
public class RobeOfMirrors extends Card {

    public RobeOfMirrors() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, Scope.ENCHANTED_CREATURE));
    }
}
