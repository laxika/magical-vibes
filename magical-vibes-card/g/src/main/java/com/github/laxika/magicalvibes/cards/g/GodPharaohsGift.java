package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "161")
@CardRegistration(set = "AKR", collectorNumber = "272")
@CardRegistration(set = "DRC", collectorNumber = "131")
public class GodPharaohsGift extends Card {

    public GodPharaohsGift() {
        // At the beginning of combat on your turn, you may exile a creature card from your graveyard.
        // If you do, create a token that's a copy of that card, except it's a 4/4 black Zombie.
        // It gains haste until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect());
    }
}
