package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.ChangeAllTargetsOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySingleCreaturePredicate;

@CardRegistration(set = "PLC", collectorNumber = "76")
public class MuckDrubb extends Card {

    public MuckDrubb() {
        addCastingOption(new MadnessCast("{2}{B}"));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsOnlySingleCreaturePredicate(),
                "Target spell must target only a single creature."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChangeAllTargetsOfTargetSpellToSourceEffect());
    }
}
