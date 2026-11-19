package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LibraryDecisionMaker;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "STH", collectorNumber = "39")
@CardRegistration(set = "S99", collectorNumber = "47")
public class Ransack extends Card {

    public Ransack() {
        addSpellEffect(new ScryEffect(new Fixed(5), Subjects.targetPlayer(), false,
                LibraryDecisionMaker.CONTROLLER));
    }
}
