package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "101")
@CardRegistration(set = "PIO", collectorNumber = "100")
public class ObNixilissCruelty extends Card {

    public ObNixilissCruelty() {
        target(TargetFilters.creature())
                .addSpellEffect(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addSpellEffect(new BoostTargetCreatureEffect(-5, -5));
    }
}
