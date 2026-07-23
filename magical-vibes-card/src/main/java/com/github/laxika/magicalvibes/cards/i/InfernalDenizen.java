package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayGainControlOfCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "136")
public class InfernalDenizen extends Card {

    public InfernalDenizen() {
        // At the beginning of your upkeep, sacrifice two Swamps. If you can't, tap this creature,
        // and an opponent may gain control of a creature you control of their choice for as long
        // as this creature remains on the battlefield.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                List.of(
                        new TapPermanentsEffect(TapUntapScope.SELF),
                        new OpponentMayGainControlOfCreatureYouControlEffect(
                                ControlDuration.WHILE_SOURCE_REMAINS))));

        // {T}: Gain control of target creature for as long as this creature remains on the battlefield.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS)),
                "{T}: Gain control of target creature for as long as this creature remains on the battlefield.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
