package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "59")
public class NewArgive extends Card {

    public NewArgive() {
        addTrigger(Triggers.creatureYouControlAttacks().withFilter(new PermanentIsHistoricPredicate()),
                new BoostTargetCreatureEffect(2, 2));
        addTrigger(Triggers.chaosEnsues(),
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardIsHistoricPredicate(), LibrarySearchDestination.HAND));
    }
}
