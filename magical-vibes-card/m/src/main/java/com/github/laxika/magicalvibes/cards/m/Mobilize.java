package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "POR", collectorNumber = "172")
public class Mobilize extends Card {

    public Mobilize() {
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentIsCreaturePredicate())));
    }
}
