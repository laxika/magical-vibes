package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "8")
public class HamatoGuardianStance extends Card {

    public HamatoGuardianStance() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.target()))
                .addSpellEffect(new ScryEffect(1));
    }
}
