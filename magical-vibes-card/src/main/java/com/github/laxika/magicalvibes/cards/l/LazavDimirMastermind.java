package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCreatureCardInOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "GTC", collectorNumber = "174")
public class LazavDimirMastermind extends Card {

    public LazavDimirMastermind() {
        // Whenever a creature card is put into an opponent's graveyard from anywhere, you may have
        // Lazav become a copy of that card, except its name is Lazav, Dimir Mastermind, it's legendary
        // in addition to its other types, and it has hexproof and this ability.
        addEffect(EffectSlot.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
                new MayEffect(new BecomeCopyOfCreatureCardInOpponentGraveyardEffect(),
                        "Become a copy of that creature card?"));
    }
}
