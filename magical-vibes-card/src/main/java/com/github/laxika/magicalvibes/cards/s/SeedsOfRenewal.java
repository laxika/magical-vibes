package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;

@CardRegistration(set = "LTC", collectorNumber = "260")
public class SeedsOfRenewal extends Card {

    public SeedsOfRenewal() {
        // Undaunted: this spell costs {1} less to cast for each opponent.
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new Sum(new PlayersInGame(), new Fixed(-1))));

        // Return up to two target cards from your graveyard to your hand.
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(null, 2));

        // Exile Seeds of Renewal.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
