package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "KHM", collectorNumber = "46")
public class BeholdTheMultiverse extends Card {

    public BeholdTheMultiverse() {
        addSpellEffect(new ScryEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addCastingOption(new ForetellCast("{1}{U}"));
    }
}
