package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "4")
public class AuriokSteelshaper extends Card {

    public AuriokSteelshaper() {
        addEffect(EffectSlot.STATIC, new ReduceEquipCostEffect(1));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Equipped(),
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER, CardSubtype.KNIGHT)))
        ));
    }
}
