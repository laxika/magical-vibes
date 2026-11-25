package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "LRW", collectorNumber = "54")
public class BrokenAmbitions extends Card {

    public BrokenAmbitions() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPaysX());
        addSpellEffect(new ClashEffect(new MillEffect(4, Subjects.controllerOf(Subjects.targetSpell()))));
    }
}
