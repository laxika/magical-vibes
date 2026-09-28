package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "1")
public class GaleaKindlerOfHope extends Card {

    public GaleaKindlerOfHope() {
        CardAnyOfPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.AURA),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(auraOrEquipment));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        auraOrEquipment,
                        List.of(new GrantTriggeredAbilityToCastSpellEffect(
                                EffectSlot.ON_ENTER_BATTLEFIELD,
                                AttachSourceEquipmentToTargetCreatureEffect.forCreatureYouControl())),
                        new StackEntryCastFromZonePredicate(Zone.LIBRARY)));
    }
}
