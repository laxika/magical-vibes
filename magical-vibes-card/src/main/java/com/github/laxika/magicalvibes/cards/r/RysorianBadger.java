package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HML", collectorNumber = "96")
public class RysorianBadger extends Card {

    public RysorianBadger() {
        // "Whenever this creature attacks and isn't blocked, you may exile up to two target creature
        // cards from defending player's graveyard. If you do, you gain 1 life for each card exiled
        // this way and this creature assigns no combat damage this turn."
        // The cards are chosen from the defending player's graveyard as the trigger goes on the
        // stack; choosing none covers the "you may", and both riders only apply when at least one
        // card was exiled.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new ExileCardsFromGraveyardEffect(2, 1, true,
                        new CardTypePredicate(CardType.CREATURE), true));
    }
}
