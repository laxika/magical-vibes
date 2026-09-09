package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "DSK", collectorNumber = "214")
public class FearOfInfinity extends Card {

    public FearOfInfinity() {
        MayEffect returnToHand = new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build(),
                "Return Fear of Infinity from your graveyard to your hand?");
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, returnToHand);
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED, returnToHand);
    }
}
