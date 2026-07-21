package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect;

@CardRegistration(set = "HOU", collectorNumber = "161")
public class GodPharaohsGift extends Card {

    public GodPharaohsGift() {
        // At the beginning of combat on your turn, you may exile a creature card from your graveyard.
        // If you do, create a token that's a copy of that card, except it's a 4/4 black Zombie.
        // It gains haste until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect());
    }
}
