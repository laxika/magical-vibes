package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDiscardedCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "HOB", collectorNumber = "163")
public class SilvanReveler extends Card {

    public SilvanReveler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardCardThenEffect(
                null,
                new ReturnDiscardedCardFromGraveyardToBattlefieldEffect(true),
                "a card",
                new CardTypePredicate(CardType.LAND)));

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayPayManaEffect("{1}{G}{U}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Pay {1}{G}{U} to return Silvan Reveler from your graveyard to your hand?"));
    }
}
