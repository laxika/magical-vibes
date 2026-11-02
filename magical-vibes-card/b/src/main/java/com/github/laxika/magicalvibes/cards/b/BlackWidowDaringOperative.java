package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "831")
public class BlackWidowDaringOperative extends Card {

    public BlackWidowDaringOperative() {
        // When Black Widow enters, mill three cards.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));

        // Whenever Black Widow deals combat damage to a player, each opponent loses life equal to
        // the number of creature cards in the controller's graveyard.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LoseLifeEffect(
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                        LoseLifeRecipient.EACH_OPPONENT));
    }
}
