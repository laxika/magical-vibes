package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

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
                        new EmpowerNextCreatureSpellThisTurnEffect(false, 2)
                ),
                "{T}: Add one mana of any color. When you next cast a creature spell this turn, that creature enters with two additional +1/+1 counters on it."
        ));

        addEffect(EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new MayEffect(
                                new PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect(),
                                "Put that many +1/+1 counters on target creature?"
                        )));
    }
}
