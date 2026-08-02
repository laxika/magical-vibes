package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "147")
public class BorborygmosEnraged extends Card {

    public BorborygmosEnraged() {
        // Whenever this deals combat damage to a player, reveal the top three cards of your library.
        // Put all land cards revealed this way into your hand and the rest into your graveyard.
        // chooseCount == lookCount makes every revealed land move to hand with no choice offered.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                3, 3, new CardTypePredicate(CardType.LAND), true));

        // Discard a land card: Borborygmos Enraged deals 3 damage to any target.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new DealDamageToAnyTargetEffect(3)),
                "Discard a land card: Borborygmos Enraged deals 3 damage to any target."));
    }
}
