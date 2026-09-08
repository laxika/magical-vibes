package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "KHM", collectorNumber = "22")
public class ResplendentMarshal extends Card {

    public ResplendentMarshal() {
        MayEffect ability = new MayEffect(
                new ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        "creature card"),
                "Exile another creature card from your graveyard?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ability);
        addEffect(EffectSlot.ON_DEATH, ability);
    }
}
