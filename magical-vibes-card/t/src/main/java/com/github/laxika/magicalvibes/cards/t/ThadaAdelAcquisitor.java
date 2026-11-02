package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WWK", collectorNumber = "40")
public class ThadaAdelAcquisitor extends Card {

    public ThadaAdelAcquisitor() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SearchTargetLibraryEffect(1, new CardTypePredicate(CardType.ARTIFACT),
                        LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_END_OF_TURN, true));
    }
}
