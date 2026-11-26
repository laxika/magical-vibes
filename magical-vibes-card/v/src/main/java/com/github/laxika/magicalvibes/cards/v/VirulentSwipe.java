package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "131")
@CardRegistration(set = "IMA", collectorNumber = "113")
public class VirulentSwipe extends Card {

    public VirulentSwipe() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.target()));
    }
}
