package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "220")
public class DrizztDoUrden extends Card {

    public DrizztDoUrden() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(CardType.CREATURE, 1, "Guenhwyvar", 4, 1,
                        CardColor.GREEN, null, List.of(CardSubtype.CAT), Set.of(Keyword.TRAMPLE), Set.of(),
                        false, false, Map.of(), List.of(), false, false, true, 0, Set.of()));

        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate()),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new Max(
                        new Fixed(0), new Sum(new EventValue(), new Scaled(new SourcePower(), -1))))));
    }
}
