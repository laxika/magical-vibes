package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DSK", collectorNumber = "207")
public class WickerfolkThresher extends Card {

    public WickerfolkThresher() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(new Delirium(),
                new LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
                        new CardTypePredicate(CardType.LAND), false)));
    }
}
