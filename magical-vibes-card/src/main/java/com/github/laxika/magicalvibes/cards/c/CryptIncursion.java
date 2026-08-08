package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DGM", collectorNumber = "23")
public class CryptIncursion extends Card {

    public CryptIncursion() {
        // Exile all creature cards from target player's graveyard. You gain 3 life for each card
        // exiled this way — the exile effect records the count on the entry's event value.
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new EventValue(), 3)));
    }
}
