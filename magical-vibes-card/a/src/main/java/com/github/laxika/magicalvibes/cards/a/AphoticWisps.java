package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "55")
public class AphoticWisps extends Card {

    public AphoticWisps() {
        target(TargetFilters.creature()).addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLACK))
          .addSpellEffect(new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET))
          .addSpellEffect(new DrawCardEffect());
    }
}
