package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "INR", collectorNumber = "164")
@CardRegistration(set = "INR", collectorNumber = "400")
@CardRegistration(set = "MRD", collectorNumber = "99")
@CardRegistration(set = "SLD", collectorNumber = "1426")
public class MassHysteria extends Card {

    public MassHysteria() {
        // All creatures have haste.
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.allCreatures()));
    }
}
