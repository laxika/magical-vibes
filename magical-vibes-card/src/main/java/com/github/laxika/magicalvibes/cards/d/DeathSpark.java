package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CardDirectlyAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ALL", collectorNumber = "70")
public class DeathSpark extends Card {

    public DeathSpark() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));

        // At the beginning of your upkeep, if this card is in your graveyard with a creature card
        // directly above it, you may pay {1}. If you do, return this card to your hand.
        // Intervening-if on the ordered graveyard is checked at trigger time.
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new CardDirectlyAboveSelfInGraveyard(new CardTypePredicate(CardType.CREATURE)),
                        new MayPayManaEffect("{1}",
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Pay {1} to return Death Spark from your graveyard to your hand?")));
    }
}
