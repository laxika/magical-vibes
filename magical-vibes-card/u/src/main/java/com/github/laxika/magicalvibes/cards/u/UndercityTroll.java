package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "202")
@CardRegistration(set = "IMA", collectorNumber = "188")
public class UndercityTroll extends Card {

    public UndercityTroll() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned())),
                new RenownEffect(1));

        // {2}{G}: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new RegenerateEffect()),
                "{2}{G}: Regenerate this creature."));
    }
}
