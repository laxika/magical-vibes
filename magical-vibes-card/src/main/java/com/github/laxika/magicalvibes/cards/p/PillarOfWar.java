package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "BNG", collectorNumber = "160")
public class PillarOfWar extends Card {

    public PillarOfWar() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Enchanted(),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
