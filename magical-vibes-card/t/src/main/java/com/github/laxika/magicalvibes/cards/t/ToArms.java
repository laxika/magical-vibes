package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "GPT", collectorNumber = "20")
@CardRegistration(set = "RVR", collectorNumber = "30")
public class ToArms extends Card {

    public ToArms() {
        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                new PermanentIsCreaturePredicate())));
        addSpellEffect(new DrawCardEffect());
    }
}
