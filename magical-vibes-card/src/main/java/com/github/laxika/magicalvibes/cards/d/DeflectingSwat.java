package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "214")
@CardRegistration(set = "CMM", collectorNumber = "532")
@CardRegistration(set = "CMM", collectorNumber = "698")
@CardRegistration(set = "TLE", collectorNumber = "311")
public class DeflectingSwat extends Card {

    public DeflectingSwat() {
        addCastingOption(new AlternateHandCast(List.of(), new ControllerControlsCommander(), false));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryIsSingleTargetPredicate(),
                "Target spell or ability must have a single target."
        )).addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
