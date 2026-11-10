package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "206")
@CardRegistration(set = "RVR", collectorNumber = "222")
public class Sharktocrab extends Card {

    public Sharktocrab() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{U}",
                List.of(new AdaptEffect(1)),
                "{2}{G}{U}: Adapt 1."
        ));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.plusOneCountersPutOnThis(),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
