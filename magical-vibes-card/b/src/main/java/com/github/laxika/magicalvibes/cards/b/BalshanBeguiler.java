package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "66")
public class BalshanBeguiler extends Card {

    public BalshanBeguiler() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LookAtTopCardsOfTargetLibraryEffect(2,
                        TargetLibraryAction.REVEAL_AND_PUT_ONE_INTO_GRAVEYARD));
    }
}
