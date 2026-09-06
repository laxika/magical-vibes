package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "122")
public class LodestoneBauble extends Card {

    public LodestoneBauble() {
        // {1}, {T}, Sacrifice this artifact: Put up to four target basic land cards from a player's
        // graveyard on top of their library in any order. That player draws a card at the beginning
        // of the next turn's upkeep.
        // The delayed draw is registered first so the graveyard's owner can still be read off the
        // targets, which the following effect moves out of the graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(),
                        RegisterDrawCardsAtNextUpkeepEffect.targetGraveyardOwner(1),
                        PutTargetCardsFromGraveyardOnTopOfLibraryEffect.fromAnyPlayerGraveyard(
                                CardPredicateUtils.basicLand(), 4)),
                "{1}, {T}, Sacrifice this artifact: Put up to four target basic land cards from a "
                        + "player's graveyard on top of their library in any order. That player draws "
                        + "a card at the beginning of the next turn's upkeep.",
                List.of(), 0, 4
        ));
    }
}
