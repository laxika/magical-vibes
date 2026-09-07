package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "69")
public class DeadlyGrub extends Card {

    public DeadlyGrub() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.TIME)),
                new CreateTokenEffect("Insect", 6, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(Keyword.SHROUD), Set.of())));
    }
}
