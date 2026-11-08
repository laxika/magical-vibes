package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "162")
@CardRegistration(set = "SPM", collectorNumber = "277")
@CardRegistration(set = "OM1", collectorNumber = "167")
public class DocOcksTentacles extends Card {

    public DocOcksTentacles() {
        addStaticEffect(new StaticBoostEffect(4, 4, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentMinManaValuePredicate(5))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                        "Attach this Equipment to it?"));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
