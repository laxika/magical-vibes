package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "50")
@CardRegistration(set = "DD2", collectorNumber = "17")
@CardRegistration(set = "MM2", collectorNumber = "39")
@CardRegistration(set = "JVC", collectorNumber = "17")
@CardRegistration(set = "MMA", collectorNumber = "36")
@CardRegistration(set = "UMA", collectorNumber = "44")
@CardRegistration(set = "2X2", collectorNumber = "37")
@CardRegistration(set = "CMD", collectorNumber = "39")
public class Aethersnipe extends Card {

    public Aethersnipe() {
        // Evoke {1}{U}{U}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}{U}"))));
        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());

        // When this creature enters, return target nonland permanent to its owner's hand.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnToHandEffect.target());
    }
}
