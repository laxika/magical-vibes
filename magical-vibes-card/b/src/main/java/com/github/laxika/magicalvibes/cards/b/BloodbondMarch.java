package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithTriggeringSpellPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "192")
public class BloodbondMarch extends Card {

    public BloodbondMarch() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        Integer.MAX_VALUE, new CardSharesNameWithTriggeringSpellPredicate()));
    }
}
