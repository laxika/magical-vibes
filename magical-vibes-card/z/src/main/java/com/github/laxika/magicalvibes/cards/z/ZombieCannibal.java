package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ODY", collectorNumber = "169")
public class ZombieCannibal extends Card {

    public ZombieCannibal() {
        // Whenever this creature deals combat damage to a player, you may exile target card from that
        // player's graveyard. The target is required, so it is chosen from the damaged player's graveyard
        // as the trigger goes on the stack; the "you may" is decided on resolution.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD),
                        "Exile the chosen card from that player's graveyard?"));
    }
}
