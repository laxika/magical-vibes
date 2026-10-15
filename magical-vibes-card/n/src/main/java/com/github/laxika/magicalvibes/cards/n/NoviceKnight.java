package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "30")
public class NoviceKnight extends Card {

    public NoviceKnight() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyOf(List.of(new Enchanted(), new Equipped())),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
