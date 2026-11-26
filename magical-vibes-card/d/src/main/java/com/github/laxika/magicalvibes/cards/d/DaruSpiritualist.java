package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "5")
public class DaruSpiritualist extends Card {

    public DaruSpiritualist() {
        addTrigger(Triggers.creatureBecomesTargetOfSpellOrAbility()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.CLERIC)))),
                BoostEffect.target(0, 2));
    }
}
