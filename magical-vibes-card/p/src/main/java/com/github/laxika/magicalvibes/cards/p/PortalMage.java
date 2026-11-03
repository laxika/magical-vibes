package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DuringDeclareAttackers;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectAttackingCreatureAttackTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "112")
@CardRegistration(set = "C20", collectorNumber = "122")
@CardRegistration(set = "C17", collectorNumber = "13")
public class PortalMage extends Card {

    public PortalMage() {
        CardEffect ability = new MayEffect(
                        new ReselectAttackingCreatureAttackTargetEffect(),
                        "Reselect the attacking creature's attack target?");
        target(TargetFilters.attackingCreature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new DuringDeclareAttackers()),
                ability);
    }
}
