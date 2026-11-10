package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "24")
@CardRegistration(set = "GN2", collectorNumber = "8")
public class KytheonsIrregulars extends Card {

    public KytheonsIrregulars() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned())),
                new RenownEffect(1));

        // {W}{W}: Tap target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{W}{W}: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
