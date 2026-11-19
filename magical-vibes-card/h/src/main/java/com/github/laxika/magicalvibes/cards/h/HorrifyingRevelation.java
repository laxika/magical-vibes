package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MBS", collectorNumber = "45")
public class HorrifyingRevelation extends Card {

    public HorrifyingRevelation() {
        addSpellEffect(new DiscardEffect(1, Subjects.targetPlayer()));
        addSpellEffect(new MillEffect(1, Subjects.targetPlayer()));
    }
}
