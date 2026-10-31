package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "77")
@CardRegistration(set = "DBL", collectorNumber = "344")
public class SerpentineAmbush extends Card {

    public SerpentineAmbush() {
        // Until end of turn, target creature becomes a blue Serpent with base power and toughness 5/5.
        target(TargetFilters.creature())
                .addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE))
                .addSpellEffect(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.SERPENT))
                .addSpellEffect(new SetBasePowerToughnessEffect(5, 5));
    }
}
