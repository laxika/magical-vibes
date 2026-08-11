package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "ODY", collectorNumber = "169")
public class ZombieCannibal extends Card {

    public ZombieCannibal() {
        // Whenever this creature deals combat damage to a player, you may exile target card from
        // that player's graveyard. The up-to-one choice models the optional target selection.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileCardsFromGraveyardEffect(1, 0));
    }
}
