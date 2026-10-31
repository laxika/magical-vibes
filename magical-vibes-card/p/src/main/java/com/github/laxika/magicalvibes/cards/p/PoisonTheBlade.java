package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "220")
public class PoisonTheBlade extends Card {

    public PoisonTheBlade() {
        // Target creature gains deathtouch until end of turn.
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))
                // Draw a card.
                .addSpellEffect(new DrawCardEffect(1));
    }
}
