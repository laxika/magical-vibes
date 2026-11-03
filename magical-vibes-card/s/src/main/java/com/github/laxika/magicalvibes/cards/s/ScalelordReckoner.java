package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "151")
@CardRegistration(set = "C17", collectorNumber = "6")
public class ScalelordReckoner extends Card {

    public ScalelordReckoner() {
        target(TargetFilters.nonlandPermanentAnOpponentControls()).addTrigger(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                new DestroyTargetPermanentEffect());
    }
}
