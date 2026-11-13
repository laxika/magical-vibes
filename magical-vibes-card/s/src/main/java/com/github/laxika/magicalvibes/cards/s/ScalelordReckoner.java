package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "151")
@CardRegistration(set = "C17", collectorNumber = "6")
public class ScalelordReckoner extends Card {

    public ScalelordReckoner() {
        // "Destroy target nonland permanent that player controls": the controller of the spell or ability that targeted.
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentControlledByTriggeringPlayerPredicate())),
                "Target must be a nonland permanent that player controls")).addTrigger(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                new DestroyTargetPermanentEffect());
    }
}
