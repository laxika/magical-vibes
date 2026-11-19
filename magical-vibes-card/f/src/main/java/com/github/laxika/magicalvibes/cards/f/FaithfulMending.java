package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MID", collectorNumber = "221")
@CardRegistration(set = "DBL", collectorNumber = "221")
public class FaithfulMending extends Card {

    public FaithfulMending() {
        addSpellEffect(new GainLifeEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, Subjects.you()));
        addCastingOption(new FlashbackCast("{1}{W}{U}"));
    }
}
