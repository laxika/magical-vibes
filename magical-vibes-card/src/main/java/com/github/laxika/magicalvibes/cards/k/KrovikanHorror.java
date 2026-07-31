package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.CardDirectlyAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "53")
public class KrovikanHorror extends Card {

    public KrovikanHorror() {
        // At the beginning of the end step, if this card is in your graveyard with a creature card
        // directly above it, you may return this card to your hand. Intervening-if on the ordered
        // graveyard is checked at trigger time.
        addEffect(EffectSlot.GRAVEYARD_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new CardDirectlyAboveSelfInGraveyard(new CardTypePredicate(CardType.CREATURE)),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Return Krovikan Horror from your graveyard to your hand?")));

        // {1}, Sacrifice a creature: Krovikan Horror deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "{1}, Sacrifice a creature: Krovikan Horror deals 1 damage to any target."
        ));
    }
}
