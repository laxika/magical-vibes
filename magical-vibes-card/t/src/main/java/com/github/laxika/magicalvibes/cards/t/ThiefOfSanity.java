package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "205")
@CardRegistration(set = "OTC", collectorNumber = "243")
public class ThiefOfSanity extends Card {

    public ThiefOfSanity() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LookAtTopCardsOfTargetLibraryEffect(3,
                        TargetLibraryAction.EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD));
    }
}
