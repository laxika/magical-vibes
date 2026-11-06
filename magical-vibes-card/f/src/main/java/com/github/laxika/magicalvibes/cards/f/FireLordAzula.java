package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "TLA", collectorNumber = "220")
public class FireLordAzula extends Card {

    public FireLordAzula() {
        addTrigger(Triggers.attacks(), new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 2));
        addTrigger(Triggers.youCastSpell().anySpell(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsAttacking()),
                new CopyTriggeringSpellEffect());
    }
}
