package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "59")
@CardRegistration(set = "DDI", collectorNumber = "4")
public class CoralFighters extends Card {

    public CoralFighters() {
        // Whenever this creature attacks and isn't blocked, look at the top card of defending
        // player's library. You may put that card on the bottom of that player's library.
        // The trigger's non-targeting targetId is the defending player.
        addTrigger(Triggers.attacksUnblocked(),
                new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.MAY_PUT_TOP_ON_BOTTOM));
    }
}
