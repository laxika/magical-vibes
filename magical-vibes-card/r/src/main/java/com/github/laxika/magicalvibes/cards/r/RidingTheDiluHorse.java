package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PTK", collectorNumber = "144")
@CardRegistration(set = "ME3", collectorNumber = "131")
public class RidingTheDiluHorse extends Card {

    public RidingTheDiluHorse() {
        // Target creature gets +2/+2 and gains horsemanship. (This effect lasts indefinitely.)
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2, Duration.PERMANENT))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HORSEMANSHIP, Subjects.target(), Duration.PERMANENT));
    }
}
