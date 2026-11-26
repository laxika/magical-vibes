package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "132")
public class WildRide extends Card {

    public WildRide() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()));
        addCastingOption(new HarmonizeCast("{4}{R}"));
    }
}
