package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToCreatureThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1737")
@CardRegistration(set = "MAR", collectorNumber = "97")
public class WolverineBestThereIs extends Card {

    public WolverineBestThereIs() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromSelfEffect());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new SelfDealtDamageToCreatureThisTurn(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new RegenerateEffect()), "{1}{G}: Regenerate Wolverine."));
    }
}
