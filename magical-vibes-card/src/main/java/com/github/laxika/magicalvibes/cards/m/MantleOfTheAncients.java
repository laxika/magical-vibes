package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "8")
@CardRegistration(set = "PIP", collectorNumber = "165")
@CardRegistration(set = "PIP", collectorNumber = "452")
@CardRegistration(set = "PIP", collectorNumber = "693")
@CardRegistration(set = "PIP", collectorNumber = "980")
public class MantleOfTheAncients extends Card {

    public MantleOfTheAncients() {
        target(TargetFilters.creatureYouControl());
        CardAnyOfPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.AURA),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.anyNumberAttachedToSourceHost(
                        auraOrEquipment));

        Scaled onePerAttachment = new Scaled(new AttachmentsOnSource(true, true), 1);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                onePerAttachment, onePerAttachment, GrantScope.ENCHANTED_CREATURE, true));
    }
}
