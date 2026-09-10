package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "207")
public class SwarmShambler extends Card {

    public SwarmShambler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.INSECT), Set.of(), Set.of())));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}, {T}: Put a +1/+1 counter on Swarm Shambler."
        ));
    }
}
