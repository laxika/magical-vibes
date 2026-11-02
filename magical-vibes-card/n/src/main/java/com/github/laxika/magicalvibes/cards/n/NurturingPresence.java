package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "26")
@CardRegistration(set = "DBL", collectorNumber = "293")
public class NurturingPresence extends Card {

    public NurturingPresence() {
        // Enchanted creature has "Whenever a creature you control enters, this creature gets +1/+1
        // until end of turn."
        target(TargetFilters.creature())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                        new BoostSelfEffect(1, 1)),
                        GrantScope.ENCHANTED_CREATURE));

        // When this Aura enters, create a 1/1 white Spirit creature token with flying.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSpirit(1));
    }
}
