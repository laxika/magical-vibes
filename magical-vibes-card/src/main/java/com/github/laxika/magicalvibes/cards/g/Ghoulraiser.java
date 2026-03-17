package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "102")
public class Ghoulraiser extends Card {

    public Ghoulraiser() {
        // When Ghoulraiser enters the battlefield, return a Zombie card at random
        // from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardSubtypePredicate(CardSubtype.ZOMBIE))
                .returnAtRandom(true)
                .build());
    }
}
