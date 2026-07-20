package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "238")
public class WatchersOfTheDead extends Card {

    public WatchersOfTheDead() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileSelfCost(),
                        new ExileGraveyardCardsEffect(2, GraveyardExileScope.EACH_OPPONENT_KEEP)),
                "Exile Watchers of the Dead: Each opponent chooses two cards in their graveyard and exiles the rest."
        ));
    }
}
