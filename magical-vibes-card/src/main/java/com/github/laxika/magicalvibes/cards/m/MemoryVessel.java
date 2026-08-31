package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayCardsFromHandUntilNextTurnEffect;
import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "13")
public class MemoryVessel extends Card {

    public MemoryVessel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileSelfCost(),
                        new EachPlayerExilesTopCardsMayPlayUntilNextTurnEffect(7),
                        new PlayersCantPlayCardsFromHandUntilNextTurnEffect()),
                "{T}, Exile this artifact: Each player exiles the top seven cards of their library. "
                        + "Until your next turn, players may play cards they exiled from their library "
                        + "this way, and they can't play cards from their hand. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
