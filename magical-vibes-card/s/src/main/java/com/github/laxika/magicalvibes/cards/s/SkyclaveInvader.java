package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class SkyclaveInvader extends Card {

    public SkyclaveInvader() {
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
                        new CardTypePredicate(CardType.LAND), false));
    }
}
