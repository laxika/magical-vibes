package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "NPH", collectorNumber = "141")
public class IsolationCell extends Card {

    public IsolationCell() {
        // Whenever an opponent casts a creature spell, that player loses 2 life unless they pay {2}.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new LoseLifeUnlessPaysEffect(2, 2, new CardTypePredicate(CardType.CREATURE)));
    }
}
