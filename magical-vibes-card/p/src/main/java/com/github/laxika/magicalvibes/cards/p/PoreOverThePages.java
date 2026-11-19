package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOI", collectorNumber = "79")
@CardRegistration(set = "DDQ", collectorNumber = "28")
@CardRegistration(set = "SIR", collectorNumber = "86")
public class PoreOverThePages extends Card {

    public PoreOverThePages() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new UntapPermanentsEffect(
                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsLandPredicate()), null, 2));
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
    }
}
