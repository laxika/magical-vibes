package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "BOK", collectorNumber = "84")
public class Skullsnatcher extends Card {

    public Skullsnatcher() {
        addNinjutsu("{B}");

        // "Whenever this creature deals combat damage to a player, exile up to two target cards from
        // that player's graveyard." The targets are chosen from the damaged player's graveyard as
        // the trigger goes on the stack; "up to two" allows choosing zero.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileCardsFromGraveyardEffect(2, 0));
    }
}
