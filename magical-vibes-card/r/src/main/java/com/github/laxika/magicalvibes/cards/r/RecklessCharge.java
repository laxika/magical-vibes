package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "215")
@CardRegistration(set = "HOP", collectorNumber = "61")
@CardRegistration(set = "EMA", collectorNumber = "144")
@CardRegistration(set = "VMA", collectorNumber = "181")
@CardRegistration(set = "MH1", collectorNumber = "144")
@CardRegistration(set = "UMA", collectorNumber = "143")
public class RecklessCharge extends Card {

    public RecklessCharge() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()));
        addCastingOption(new FlashbackCast("{2}{R}"));
    }
}
