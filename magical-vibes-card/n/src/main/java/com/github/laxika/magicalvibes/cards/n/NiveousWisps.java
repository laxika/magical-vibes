package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "15")
public class NiveousWisps extends Card {

    public NiveousWisps() {
        target(TargetFilters.creature()).addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.WHITE))
          .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
          .addSpellEffect(new DrawCardEffect());
    }
}
