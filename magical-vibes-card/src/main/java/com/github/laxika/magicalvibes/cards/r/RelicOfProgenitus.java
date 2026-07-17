package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "218")
public class RelicOfProgenitus extends Card {

    public RelicOfProgenitus() {
        // {T}: Target player exiles a card from their graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TargetPlayerExilesCardFromGraveyardEffect(0)),
                "{T}: Target player exiles a card from their graveyard."
        ));

        // {1}, Exile Relic of Progenitus: Exile all graveyards. Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new ExileSelfCost(),
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS),
                        new DrawCardEffect(1)
                ),
                "{1}, Exile Relic of Progenitus: Exile all graveyards. Draw a card."
        ));
    }
}
