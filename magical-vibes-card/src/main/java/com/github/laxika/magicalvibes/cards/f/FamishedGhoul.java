package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "135")
public class FamishedGhoul extends Card {

    public FamishedGhoul() {
        // {1}{B}, Sacrifice this creature: Exile up to two target cards from a single graveyard.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeSelfCost(),
                        new ExileGraveyardCardsEffect(2, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{1}{B}, Sacrifice this creature: Exile up to two target cards from a single graveyard."
        ));
    }
}
