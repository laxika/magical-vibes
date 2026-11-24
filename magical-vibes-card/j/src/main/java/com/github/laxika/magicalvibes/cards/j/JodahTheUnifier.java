package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1932")
@CardRegistration(set = "DMU", collectorNumber = "203")
@CardRegistration(set = "FCA", collectorNumber = "17")
public class JodahTheUnifier extends Card {

    /** "For each other legendary creature you control", counted for each boosted creature. */
    private static final PermanentCount OTHER_LEGENDARY_CREATURES_YOU_CONTROL = new PermanentCount(
            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                    new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
            CountScope.CONTROLLER, true);

    public JodahTheUnifier() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        addStaticEffect(new DynamicStaticBoostEffect(
                OTHER_LEGENDARY_CREATURES_YOU_CONTROL, OTHER_LEGENDARY_CREATURES_YOU_CONTROL,
                Subjects.creaturesYouControl(), new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY), true));

        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardSupertypePredicate(CardSupertype.LEGENDARY))
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.HAND)),
                new CascadeEffect(false, new CardSupertypePredicate(CardSupertype.LEGENDARY), false, true));
    }
}
