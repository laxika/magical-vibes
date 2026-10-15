package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20ForEachPlayerAndRestrictSourceAttacksEffect;

@CardRegistration(set = "AFC", collectorNumber = "30")
public class ChaosDragon extends Card {

    public ChaosDragon() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new RollD20ForEachPlayerAndRestrictSourceAttacksEffect());
    }
}
