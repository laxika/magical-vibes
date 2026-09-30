package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "320")
@CardRegistration(set = "MB2", collectorNumber = "104")
public class BasiliskGate extends Card {

    public BasiliskGate() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: Target creature gets +X/+X until end of turn, where X is the number of Gates
        // you control. Activate only as a sorcery.
        PermanentCount gatesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostTargetCreatureEffect(gatesYouControl, gatesYouControl)),
                "{2}, {T}: Target creature gets +X/+X until end of turn, where X is the number of "
                        + "Gates you control. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
