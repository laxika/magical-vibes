package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.ChangeAllTargetsOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySingleCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "76")
@CardRegistration(set = "TSR", collectorNumber = "126")
public class MuckDrubb extends Card {

    public MuckDrubb() {
        addCastingOption(new MadnessCast("{2}{B}"));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsOnlySingleCreaturePredicate(),
                "Target spell must target only a single creature."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChangeAllTargetsOfTargetSpellToSourceEffect());
    }
}
