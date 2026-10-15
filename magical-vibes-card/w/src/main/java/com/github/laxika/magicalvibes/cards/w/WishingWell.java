package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "81")
public class WishingWell extends Card {

    public WishingWell() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnSelfThenCastTargetInstantOrSorceryFromGraveyardEffect(
                        CounterType.COIN,
                        true)),
                "{T}: Put a coin counter on Wishing Well. When you do, you may cast target instant or sorcery "
                        + "card with mana value equal to the number of coin counters on Wishing Well from your "
                        + "graveyard without paying its mana cost. If that spell would be put into your graveyard, "
                        + "exile it instead.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
