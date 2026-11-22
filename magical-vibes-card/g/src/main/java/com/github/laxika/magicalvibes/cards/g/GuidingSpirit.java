package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "131")
public class GuidingSpirit extends Card {

    public GuidingSpirit() {
        // {T}: If the top card of target player's graveyard is a creature card, put that card on top of that player's library.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PutCardsIntoLibraryEffect.onTop(Subjects.topOfGraveyard(
                        Subjects.targetPlayer(), new CardTypePredicate(CardType.CREATURE)))),
                "{T}: If the top card of target player's graveyard is a creature card, put that card on top of that player's library."));
    }
}
