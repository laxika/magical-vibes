package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "8")
@CardRegistration(set = "OPCA", collectorNumber = "22")
public class TheEonFog extends Card {

    public TheEonFog() {
        addStaticEffect(new PlayersSkipUntapStepEffect());
        addTrigger(Triggers.chaosEnsues(), new UntapPermanentsEffect(TapUntapScope.CONTROLLED));
    }
}
