package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceStartedTurnUntapped;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterLimitEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCappedCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "253")
public class RasputinDreamweaver extends Card {

    public RasputinDreamweaver() {
        addEffect(EffectSlot.STATIC, new CounterLimitEffect(CounterType.DREAM, 7));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.DREAM, new Fixed(7)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.DREAM),
                        new AwardManaEffect(ManaColor.COLORLESS)
                ),
                "Remove a dream counter from Rasputin: Add {C}."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.DREAM),
                        PreventDamageEffect.nextToSelf(1)
                ),
                "Remove a dream counter from Rasputin: Prevent the next 1 damage that would be dealt to Rasputin this turn."
        ));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceStartedTurnUntapped(),
                        new PutCappedCountersOnSourceEffect(CounterType.DREAM, new Fixed(1), 7)));
    }
}
