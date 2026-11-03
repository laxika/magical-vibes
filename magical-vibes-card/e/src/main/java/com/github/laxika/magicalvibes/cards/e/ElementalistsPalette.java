package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "346")
@CardRegistration(set = "C21", collectorNumber = "76")
public class ElementalistsPalette extends Card {

    public ElementalistsPalette() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryHasXInManaCostPredicate()),
                new PutCountersOnSelfEffect(CounterType.CHARGE, 2));

        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS,
                        new CountersOnSource(CounterType.CHARGE),
                        new ManaRestriction.XCosts())),
                "{T}: Add {C} for each charge counter on this artifact. Spend this mana only on costs that contain {X}."
        ));
    }
}
