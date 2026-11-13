package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1552")
@CardRegistration(set = "CMM", collectorNumber = "214")
@CardRegistration(set = "CMM", collectorNumber = "532")
@CardRegistration(set = "CMM", collectorNumber = "698")
@CardRegistration(set = "TLE", collectorNumber = "311")
@CardRegistration(set = "C20", collectorNumber = "50")
public class DeflectingSwat extends Card {

    public DeflectingSwat() {
        addCastingOption(new AlternateHandCast(List.of(), new ControlledCommanderAsCast(), false));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(StackEntryType.SPELLS_AND_ABILITIES),
                "Target must be a spell or ability."
        )).addSpellEffect(new MayEffect(new ChooseNewTargetsForTargetSpellEffect(), "Choose new targets?"));
    }
}
