package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "80")
public class CovetousDragon extends Card {

    public CovetousDragon() {
        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentAllOfPredicate(List.of(new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsArtifactPredicate()))),
                new SacrificeSelfEffect());
    }
}
