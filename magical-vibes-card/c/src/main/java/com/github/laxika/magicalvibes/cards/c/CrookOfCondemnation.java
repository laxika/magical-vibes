package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "159")
public class CrookOfCondemnation extends Card {

    public CrookOfCondemnation() {
        // {1}, {T}: Exile target card from a graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{1}, {T}: Exile target card from a graveyard."
        ));

        // {1}, Exile this artifact: Exile all graveyards.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new ExileSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS)
                ),
                "{1}, Exile this artifact: Exile all graveyards."
        ));
    }
}
