package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "119")
public class DredgingClaw extends Card {

    public DredgingClaw() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.entersFrom(Zone.GRAVEYARD, Triggers.CREATURE, ObjectRelation.ANY),
                new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                        "Attach this Equipment to it?"));
        addActivatedAbility(new EquipActivatedAbility("{1}{B}"));
    }
}
