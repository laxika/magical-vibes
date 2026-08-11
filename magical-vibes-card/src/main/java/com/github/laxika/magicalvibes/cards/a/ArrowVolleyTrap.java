package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "2")
public class ArrowVolleyTrap extends Card {

    public ArrowVolleyTrap() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{W}")),
                new AnyPlayerControlsPermanentCount(4, new PermanentIsAttackingPredicate()),
                false));
        addEffect(EffectSlot.SPELL, new DealDividedDamageEffect(
                new Fixed(5),
                null,
                DivisionMode.CHOSEN,
                new PermanentIsAttackingPredicate(),
                0,
                false,
                false,
                false
        ));
    }
}
