package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "40")
@CardRegistration(set = "PIO", collectorNumber = "46")
public class ArtisanOfForms extends Card {

    public ArtisanOfForms() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                BecomeCopyOfTargetCreatureEffect.retainingItsAbility());
    }
}
