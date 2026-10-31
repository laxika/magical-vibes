package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "7")
@CardRegistration(set = "M21", collectorNumber = "15")
@CardRegistration(set = "WAR", collectorNumber = "9")
@CardRegistration(set = "STA", collectorNumber = "3")
public class DefiantStrike extends Card {

    public DefiantStrike() {
        // Target creature gets +1/+0 until end of turn.
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(1, 0))
          // Draw a card.
          .addSpellEffect(new DrawCardEffect(1));
    }
}
