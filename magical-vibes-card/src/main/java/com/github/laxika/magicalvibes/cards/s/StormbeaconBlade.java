package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "TDM", collectorNumber = "27")
public class StormbeaconBlade extends Card {

    public StormbeaconBlade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumMatchingAttackers(3, new PermanentTruePredicate()),
                new DrawCardEffect()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
