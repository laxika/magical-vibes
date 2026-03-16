package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "ISD", collectorNumber = "137")
public class CurseOfTheNightlyHunt extends Card {

    public CurseOfTheNightlyHunt() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect(GrantScope.ENCHANTED_PLAYER_CREATURES));
    }
}
