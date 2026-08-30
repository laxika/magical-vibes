package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MAT", collectorNumber = "23")
public class OpenTheWay extends Card {

    public OpenTheWay() {
        setXValueCap(new PlayersInGame());
        addEffect(EffectSlot.SPELL,
                RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect
                        .allMatchingOntoBattlefieldTapped(new XValue(), new CardTypePredicate(CardType.LAND)));
    }
}
