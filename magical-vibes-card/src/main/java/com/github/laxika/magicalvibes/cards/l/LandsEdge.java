package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "52")
public class LandsEdge extends Card {

    public LandsEdge() {
        // Discard a card: If the discarded card was a land card, this enchantment deals 2 damage
        // to target player or planeswalker. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null, false, 1, false, false, true),
                        new ConditionalEffect(
                                new ImprintedCardMatches(new CardTypePredicate(CardType.LAND), "a land card",
                                        "discarded card"),
                                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2))
                ),
                "Discard a card: If the discarded card was a land card, this enchantment deals 2 damage "
                        + "to target player or planeswalker. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
