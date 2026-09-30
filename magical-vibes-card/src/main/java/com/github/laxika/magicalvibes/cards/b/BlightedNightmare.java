package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BlightXCost;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "8")
public class BlightedNightmare extends Card {

    public BlightedNightmare() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyBoostCreatureCardsInGraveyardEffect(1, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new BlightXCost(new GreatestToughnessAmongControlled()),
                        new ReturnSelfToHandCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .requiresManaValueAtMostX(true)
                                .build()),
                "Blight X, Return this enchantment to its owner's hand: Return target creature card with mana value X or less from your graveyard to the battlefield. X can't be greater than the greatest toughness among creatures you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
