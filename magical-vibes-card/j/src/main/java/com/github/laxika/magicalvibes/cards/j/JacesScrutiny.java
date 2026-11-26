package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "70")
@CardRegistration(set = "SIR", collectorNumber = "78")
public class JacesScrutiny extends Card {

    public JacesScrutiny() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-4, 0))
                .addSpellEffect(CreateTokenEffect.ofClueToken(1));
    }
}
