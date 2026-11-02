package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "35")
@CardRegistration(set = "FIN", collectorNumber = "358")
public class SummonChocoMog extends Card {

    public SummonChocoMog() {
        var otherCreatures = new PermanentNotPredicate(new PermanentIsSourceCardPredicate());
        var stampede = new BoostAllOwnCreaturesEffect(1, 0, otherCreatures);
        addTrigger(Triggers.chapter(1), stampede);
        addTrigger(Triggers.chapter(2), stampede);
        addTrigger(Triggers.chapter(3), stampede);
        addTrigger(Triggers.chapter(4), stampede);
    }
}
