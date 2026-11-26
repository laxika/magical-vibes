package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "69")
@CardRegistration(set = "DMU", collectorNumber = "64")
public class ShoreUp extends Card {

    public ShoreUp() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(BoostEffect.target(1, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.target()))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
