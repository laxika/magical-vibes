package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "66")
@CardRegistration(set = "WOE", collectorNumber = "334")
public class SleepCursedFaerie extends Card {

    public SleepCursedFaerie() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessPays(2));
        addStaticEffect(new EnterWithCountersEffect(CounterType.STUN, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new UntapPermanentsEffect(Subjects.source())),
                "{1}{U}: Untap this creature."
        ));
    }
}
