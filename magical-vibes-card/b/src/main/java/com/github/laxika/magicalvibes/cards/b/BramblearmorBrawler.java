package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "16")
public class BramblearmorBrawler extends Card {

    public BramblearmorBrawler() {
        addStaticEffect(new CantBeCounteredEffect());
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PerpetuallyBoostCreatureCardsInLibraryEffect(1, 1));
    }
}
