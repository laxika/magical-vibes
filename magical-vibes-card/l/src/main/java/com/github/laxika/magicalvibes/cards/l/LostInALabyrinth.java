package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "52")
public class LostInALabyrinth extends Card {

    public LostInALabyrinth() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-3, 0))
                .addSpellEffect(new ScryEffect(1));
    }
}
