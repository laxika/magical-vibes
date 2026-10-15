package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToOwnPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "143")
public class PushTheLimit extends Card {

    public PushTheLimit() {
        CardAnyOfPredicate mountOrVehicle = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.MOUNT),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(mountOrVehicle)
                .returnAll(true)
                .sacrificeAtEndStep(true)
                .build());
        addEffect(EffectSlot.SPELL, new AddCardTypeToOwnPermanentsUntilEndOfTurnEffect(
                CardType.CREATURE, new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_CREATURES));
    }
}
