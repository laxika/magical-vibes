package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TimmerianFiendsAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "58")
public class TimmerianFiends extends Card {

    public TimmerianFiends() {
        // "Remove this card from your deck before playing if you're not playing for ante" is a
        // deck-construction instruction with no in-game effect.

        // {B}{B}{B}, Sacrifice this creature: The owner of target artifact may ante the top card of
        // their library. If that player doesn't, exchange ownership of that artifact and Timmerian
        // Fiends. Put the artifact card into your graveyard and Timmerian Fiends from anywhere into
        // that player's graveyard. This change in ownership is permanent.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}{B}",
                List.of(new SacrificeSelfCost(), new TimmerianFiendsAnteExchangeEffect()),
                "{B}{B}{B}, Sacrifice Timmerian Fiends: The owner of target artifact may ante the top "
                        + "card of their library. If that player doesn't, exchange ownership of that "
                        + "artifact and Timmerian Fiends. Put the artifact card into your graveyard and "
                        + "Timmerian Fiends from anywhere into that player's graveyard. This change in "
                        + "ownership is permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));
    }
}
