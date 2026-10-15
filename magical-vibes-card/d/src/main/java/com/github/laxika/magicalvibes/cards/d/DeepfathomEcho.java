package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;


@CardRegistration(set = "LCI", collectorNumber = "228")
@CardRegistration(set = "LCI", collectorNumber = "385")
public class DeepfathomEcho extends Card {

    public DeepfathomEcho() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new ExploreEffect(),
                new MayEffect(
                        new BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect(),
                        "Have Deepfathom Echo become a copy of another creature you control until end of turn?"
                )
        ));
    }
}
