package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "41")
public class HuntersBow extends Card {

    public HuntersBow() {
        var attachSourceEquipmentToTargetCreature = new AttachSourceEquipmentToTargetCreatureEffect();
        target(TargetFilters.creatureYouControl())
                .bindEffect(attachSourceEquipmentToTargetCreature);
        var targetDealsPowerDamageToTarget = DealDamageEffect.bite();
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .bindEffect(targetDealsPowerDamageToTarget);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                attachSourceEquipmentToTargetCreature,
                targetDealsPowerDamageToTarget);

        addStaticEffect(new GrantKeywordEffect(Set.of(Keyword.REACH), Subjects.attachedCreature()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(2)), Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
