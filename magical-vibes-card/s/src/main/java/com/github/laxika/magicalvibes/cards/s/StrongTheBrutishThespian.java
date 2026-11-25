package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GiveRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RadiationLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "84")
@CardRegistration(set = "PIP", collectorNumber = "403")
@CardRegistration(set = "PIP", collectorNumber = "612")
@CardRegistration(set = "PIP", collectorNumber = "931")
public class StrongTheBrutishThespian extends Card {

    public StrongTheBrutishThespian() {
        addTrigger(Triggers.dealtDamage(), SequenceEffect.of(
                new GiveRadCountersEffect(3, Subjects.you()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 3)));
        addStaticEffect(new RadiationLifeGainReplacementEffect());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));
    }
}
