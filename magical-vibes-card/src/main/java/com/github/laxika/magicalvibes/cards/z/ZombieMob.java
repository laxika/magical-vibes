package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MIR", collectorNumber = "153")
public class ZombieMob extends Card {

    public ZombieMob() {
        // This creature enters with a +1/+1 counter on it for each creature card in your graveyard.
        // Applied as an as-enters replacement, so the count is taken before the exile trigger resolves.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER)));

        // When this creature enters, exile all creature cards from your graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.OWN_ALL_MATCHING, new CardTypePredicate(CardType.CREATURE)));
    }
}
