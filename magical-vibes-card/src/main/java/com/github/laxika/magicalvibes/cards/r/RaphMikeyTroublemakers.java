package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TMT", collectorNumber = "167")
@CardRegistration(set = "TMT", collectorNumber = "250")
public class RaphMikeyTroublemakers extends Card {

    public RaphMikeyTroublemakers() {
        addEffect(EffectSlot.ON_ATTACK,
                RevealUntilCardPredicateRestOnBottomRandomEffect.tappedAndAttacking(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
