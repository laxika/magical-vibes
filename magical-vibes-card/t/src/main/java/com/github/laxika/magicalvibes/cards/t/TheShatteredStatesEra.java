package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NamelessConqueror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "162")
public class TheShatteredStatesEra extends Card {

    public TheShatteredStatesEra() {
        setBackFaceCard(new NamelessConqueror());

        target(TargetFilters.creature())
                .addTrigger(Triggers.chapter(1),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addTrigger(Triggers.chapter(1),
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.chapter(1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
        addTrigger(Triggers.chapter(2), new BoostAllOwnCreaturesEffect(1, 0));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "NamelessConqueror";
    }
}
