package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "132")
public class ReptilianReflection extends Card {

    public ReptilianReflection() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new CyclingTriggerEffect(new MayEffect(
                        new AnimatePermanentsEffect(5, 4, List.of(CardSubtype.DINOSAUR),
                                Set.of(Keyword.TRAMPLE, Keyword.HASTE)),
                        "Have Reptilian Reflection become a 5/4 Dinosaur creature with trample and haste until end of turn?")));
    }
}
