package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "80")
public class CovetousDragon extends Card {

    public CovetousDragon() {
        addTrigger(Triggers.state(), StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsArtifactPredicate())),
                List.of(new SacrificeSelfEffect()),
                "Covetous Dragon's state-triggered ability"
        ));
    }
}
