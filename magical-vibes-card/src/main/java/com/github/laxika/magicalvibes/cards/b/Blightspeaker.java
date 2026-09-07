package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "64")
public class Blightspeaker extends Card {

    public Blightspeaker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{T}: Target player loses 1 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.REBEL),
                                new CardIsPermanentPredicate(),
                                new CardMaxManaValuePredicate(3))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "{4}, {T}: Search your library for a Rebel permanent card with mana value 3 or less, "
                        + "put it onto the battlefield, then shuffle."
        ));
    }
}
