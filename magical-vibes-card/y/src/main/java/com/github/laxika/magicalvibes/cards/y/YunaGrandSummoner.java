package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.WhenYouNextCastCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "8")
@CardRegistration(set = "FIC", collectorNumber = "192")
@CardRegistration(set = "FIC", collectorNumber = "208")
@CardRegistration(set = "FIC", collectorNumber = "216")
@CardRegistration(set = "FIC", collectorNumber = "227")
public class YunaGrandSummoner extends Card {

    public YunaGrandSummoner() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(),
                        new WhenYouNextCastCreatureSpellThisTurnEffect(
                                new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(new Fixed(2)))
                ),
                "{T}: Add one mana of any color. When you next cast a creature spell this turn, that creature enters with two additional +1/+1 counters on it."
        ));

        addTrigger(Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                                new PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect(),
                                "Put that many +1/+1 counters on target creature?"
                        ));
    }
}
