package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "119")
@CardRegistration(set = "MSC", collectorNumber = "460")
public class VisionSynthezoidAvenger extends Card {

    public VisionSynthezoidAvenger() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                SpellCastTriggerEffect.whenCasterIsNotActiveTurn(List.of(
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Put a +1/+1 counter on Vision.",
                                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Vision phases out.",
                                        new PhaseOutEffect(PhaseOutSubject.SOURCE))
                        ))
                )));
    }
}
