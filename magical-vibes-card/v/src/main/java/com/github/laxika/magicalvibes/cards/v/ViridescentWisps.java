package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "132")
public class ViridescentWisps extends Card {

    public ViridescentWisps() {
        // Target creature becomes green and gets +1/+0 until end of turn. Draw a card.
        target(TargetFilters.creature()).addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.GREEN))
          .addSpellEffect(new BoostTargetCreatureEffect(1, 0))
          .addSpellEffect(new DrawCardEffect());
    }
}
