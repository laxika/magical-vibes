package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "183")
@CardRegistration(set = "KLR", collectorNumber = "198")
@CardRegistration(set = "2X2", collectorNumber = "237")
public class KambalConsulOfAllocation extends Card {

    public KambalConsulOfAllocation() {
        // Whenever an opponent casts a noncreature spell, that player loses 2 life and you gain 2 life.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER),
                new GainLifeEffect(2));
    }
}
