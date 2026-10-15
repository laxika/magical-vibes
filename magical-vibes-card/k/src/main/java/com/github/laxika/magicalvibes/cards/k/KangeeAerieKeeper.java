package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "253")
public class KangeeAerieKeeper extends Card {

    public KangeeAerieKeeper() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{X}{2}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.FEATHER, new XValue())));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(), GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.BIRD), CounterType.FEATHER));
    }
}
