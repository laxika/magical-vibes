package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "91")
@CardRegistration(set = "M15", collectorNumber = "140")
@CardRegistration(set = "SIS", collectorNumber = "41")
public class ForgeDevil extends Card {

    public ForgeDevil() {
        // When this creature enters, it deals 1 damage to target creature...
        DealDamageEffect creatureDamage = DealDamageEffect.toTargetCreature(1);
        target(TargetFilters.creature()).bindEffect(creatureDamage);

        // ...and 1 damage to you.
        addSimultaneousTrigger(Triggers.enters(ObjectRelation.THIS),
                creatureDamage,
                DealDamageEffect.to(Subjects.you(), 1));
    }
}
