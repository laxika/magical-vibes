package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "LCI", collectorNumber = "228")
@CardRegistration(set = "LCI", collectorNumber = "385")
public class DeepfathomEcho extends Card {

    public DeepfathomEcho() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new ExploreEffect(),
                new MayEffect(
                        new BecomeCopyOfChosenCreatureYouControlUntilEndOfTurnEffect(),
                        "Have Deepfathom Echo become a copy of another creature you control until end of turn?"
                )
        ));
    }
}
