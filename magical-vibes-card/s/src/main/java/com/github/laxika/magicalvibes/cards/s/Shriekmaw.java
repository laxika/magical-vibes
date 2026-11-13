package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "139")
@CardRegistration(set = "DDH", collectorNumber = "54")
@CardRegistration(set = "UMA", collectorNumber = "113")
@CardRegistration(set = "TSR", collectorNumber = "331")
@CardRegistration(set = "ECC", collectorNumber = "85")
@CardRegistration(set = "CMD", collectorNumber = "100")
@CardRegistration(set = "C14", collectorNumber = "160")
@CardRegistration(set = "C15", collectorNumber = "137")
@CardRegistration(set = "MKC", collectorNumber = "139")
@CardRegistration(set = "AFC", collectorNumber = "110")
@CardRegistration(set = "C20", collectorNumber = "136")
@CardRegistration(set = "CMA", collectorNumber = "68")
@CardRegistration(set = "ARC", collectorNumber = "24")
public class Shriekmaw extends Card {

    public Shriekmaw() {
        // Fear is auto-loaded from Scryfall keywords.
        // Evoke {1}{B}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}"))));
        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());

        // When this creature enters, destroy target nonartifact, nonblack creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonartifact, nonblack creature"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
