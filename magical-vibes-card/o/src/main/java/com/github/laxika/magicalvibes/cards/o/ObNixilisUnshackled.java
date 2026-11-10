package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "110")
@CardRegistration(set = "2X2", collectorNumber = "84")
public class ObNixilisUnshackled extends Card {

    public ObNixilisUnshackled() {
        // Whenever an opponent searches their library, that player sacrifices a creature of their
        // choice and loses 10 life. The searching player is baked in as the trigger's targetId, so
        // both TARGET_PLAYER-scoped effects act on them.
        addTrigger(Triggers.opponentSearchesTheirLibrary(), new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.TARGET_PLAYER),
                new LoseLifeEffect(10, LoseLifeRecipient.TARGET_PLAYER));

        // Whenever another creature dies, put a +1/+1 counter on Ob Nixilis.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
