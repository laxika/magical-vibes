package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "8")
public class MantleOfTheAncients extends Card {

    public MantleOfTheAncients() {
        CardAnyOfPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        new AttachmentsOnSource(true, true),
                        new AttachmentsOnSource(true, true),
                        GrantScope.ENCHANTED_CREATURE,
                        true));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.attachedToEnchantedCreature(
                        auraOrEquipment));
    }
}
