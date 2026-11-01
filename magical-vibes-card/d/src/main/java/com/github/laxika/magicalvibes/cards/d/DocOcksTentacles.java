package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "162")
@CardRegistration(set = "SPM", collectorNumber = "277")
@CardRegistration(set = "OM1", collectorNumber = "167")
public class DocOcksTentacles extends Card {

    public DocOcksTentacles() {
        addStaticEffect(new StaticBoostEffect(4, 4, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardMinManaValuePredicate(5),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
