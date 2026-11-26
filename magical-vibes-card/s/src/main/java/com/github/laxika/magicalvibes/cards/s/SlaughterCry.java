package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "155")
@CardRegistration(set = "ZEN", collectorNumber = "149")
public class SlaughterCry extends Card {

    public SlaughterCry() {
        // Target creature gets +3/+0 and gains first strike until end of turn.
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(new Fixed(3), new Fixed(0)))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
    }
}
