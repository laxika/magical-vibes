package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "167")
@CardRegistration(set = "TMT", collectorNumber = "250")
public class RaphMikeyTroublemakers extends Card {

    public RaphMikeyTroublemakers() {
        addTrigger(Triggers.attacks(),
                RevealUntilCardPredicateRestOnBottomRandomEffect.tappedAndAttacking(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
