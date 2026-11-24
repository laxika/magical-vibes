package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "E02", collectorNumber = "4")
@CardRegistration(set = "SS2", collectorNumber = "5")
@CardRegistration(set = "SOC", collectorNumber = "172")
@CardRegistration(set = "C15", collectorNumber = "8")
public class ShieldedByFaith extends Card {

    public ShieldedByFaith() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.attachedCreature()));

        // Whenever a creature enters, you may attach this Aura to that creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                        "Attach this Aura to that creature?"));
    }
}
