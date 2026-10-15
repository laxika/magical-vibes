package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DSK", collectorNumber = "2")
public class CultHealer extends Card {

    public CultHealer() {
        GrantKeywordEffect gainLifelink = new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, gainLifelink);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, gainLifelink);
    }
}
