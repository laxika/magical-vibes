package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "54")
public class EmpoweredAutogenerator extends Card {

    public EmpoweredAutogenerator() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Put a charge counter on this artifact. Add X mana of any one color, where X is the
        // number of charge counters on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCountersOnSelfEffect(CounterType.CHARGE),
                        new AwardAnyColorManaEffect(new CountersOnSource(CounterType.CHARGE))
                ),
                "{T}: Put a charge counter on this artifact. Add X mana of any one color, where X is the number of charge counters on this artifact."
        ));
    }
}
