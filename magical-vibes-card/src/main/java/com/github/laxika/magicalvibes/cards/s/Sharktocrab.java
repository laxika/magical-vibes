package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "206")
public class Sharktocrab extends Card {

    public Sharktocrab() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{U}",
                List.of(new AdaptEffect(1)),
                "{2}{G}{U}: Adapt 1."
        ));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                        new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                        new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
