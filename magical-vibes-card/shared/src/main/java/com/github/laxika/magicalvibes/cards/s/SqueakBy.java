package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class SqueakBy extends Card {

    public SqueakBy() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(1, 1))
                .addSpellEffect(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentPowerAtMostPredicate(2),
                        "creatures with power 2 or less"));
    }
}
