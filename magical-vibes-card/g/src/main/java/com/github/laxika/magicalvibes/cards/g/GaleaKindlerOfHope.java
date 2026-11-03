package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "1")
public class GaleaKindlerOfHope extends Card {

    public GaleaKindlerOfHope() {
        CardAnyOfPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.AURA),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(auraOrEquipment));
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardSubtypePredicate(CardSubtype.EQUIPMENT))
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.LIBRARY)),
                new GrantTriggeredAbilityToCastSpellEffect(
                        new TriggeredAbility(Triggers.enters(ObjectRelation.THIS),
                        AttachSourceEquipmentToTargetCreatureEffect.forCreatureYouControl())));
    }
}
