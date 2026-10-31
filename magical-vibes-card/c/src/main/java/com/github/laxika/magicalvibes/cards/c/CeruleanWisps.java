package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "31")
public class CeruleanWisps extends Card {

    public CeruleanWisps() {
        target(TargetFilters.creature()).addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
          .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET))
          .addSpellEffect(new DrawCardEffect());
    }
}
