package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "531")
public class GravitonFundamentalForce extends Card {

    public GravitonFundamentalForce() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gains flying until end of turn",
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Tap target creature",
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        TargetFilters.creature())
        )));
    }
}
