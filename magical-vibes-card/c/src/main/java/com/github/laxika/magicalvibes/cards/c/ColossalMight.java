package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ARB", collectorNumber = "51")
@CardRegistration(set = "CMD", collectorNumber = "190")
@CardRegistration(set = "ARC", collectorNumber = "83")
public class ColossalMight extends Card {

    public ColossalMight() {
        // Target creature gets +4/+2 and gains trample until end of turn.
        addSpellEffect(BoostEffect.target(4, 2));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
