package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "180")
@CardRegistration(set = "DD2", collectorNumber = "45")
@CardRegistration(set = "JVC", collectorNumber = "45")
@CardRegistration(set = "UMA", collectorNumber = "135")
@CardRegistration(set = "C14", collectorNumber = "179")
@CardRegistration(set = "ECC", collectorNumber = "95")
@CardRegistration(set = "CM2", collectorNumber = "110")
public class IngotChewer extends Card {

    public IngotChewer() {
        // Evoke {R}: cast for the alternate cost instead of the mana cost; it's sacrificed on entry.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{R}"))));
        // Evoke sacrifice: if it was cast for its evoke cost, sacrifice it as it enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeSelfIfEvokedEffect());

        // When this creature enters, destroy target artifact.
        target(TargetFilters.artifact())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
