package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromAnyLibraryTopEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "39")
@CardRegistration(set = "OPCA", collectorNumber = "85")
public class WindriddlePalaces extends Card {

    public WindriddlePalaces() {
        addStaticEffect(PlayWithTopCardRevealedEffect.forAllPlayers());
        addStaticEffect(new AllowPlayFromAnyLibraryTopEffect());
        addTrigger(Triggers.chaosEnsues(), new MillEffect(1, MillRecipient.CONTROLLER));
        addTrigger(Triggers.chaosEnsues(), new MillEffect(1, MillRecipient.EACH_OPPONENT));
    }
}
