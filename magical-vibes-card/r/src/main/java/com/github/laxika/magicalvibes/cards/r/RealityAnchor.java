package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMP", collectorNumber = "246")
@CardRegistration(set = "TPR", collectorNumber = "188")
public class RealityAnchor extends Card {

    public RealityAnchor() {
        // Target creature loses shadow until end of turn. Draw a card.
        target(TargetFilters.creature())
                .addSpellEffect(new RemoveKeywordEffect(Keyword.SHADOW, GrantScope.TARGET))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
