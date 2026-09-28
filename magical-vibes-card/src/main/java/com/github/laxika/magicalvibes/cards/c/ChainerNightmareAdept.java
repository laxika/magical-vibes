package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureNotCastFromHandConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "289")
public class ChainerNightmareAdept extends Card {

    public ChainerNightmareAdept() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                                new CardTypePredicate(CardType.CREATURE), true)
                ),
                "Discard a card: You may cast a creature spell from your graveyard this turn. "
                        + "Activate only once each turn.",
                1));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureNotCastFromHandConditionalEffect(
                        new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE),
                                GrantDuration.UNTIL_YOUR_NEXT_TURN)));
    }
}
