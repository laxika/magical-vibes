package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "91")
public class BladedBattleFan extends Card {

    public BladedBattleFan() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.attachedCreature()));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new AttachSourceEquipmentToTargetCreatureEffect(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.target())));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
