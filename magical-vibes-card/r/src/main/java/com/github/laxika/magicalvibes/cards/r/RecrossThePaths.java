package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "133")
public class RecrossThePaths extends Card {

    public RecrossThePaths() {
        var landCard = new CardTypePredicate(CardType.LAND);
        // Reveal cards from the top of your library until you reveal a land card. Put that card onto
        // the battlefield and the rest on the bottom of your library in any order. (pre-clash body)
        // Then clash with an opponent. If you win, return Recross the Paths to its owner's hand.
        addSpellEffect(new ClashEffect(
                List.<CardEffect>of(new RevealCardsFromTopUntilEffect(landCard),
                        new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(landCard)),
                        PutCardsIntoLibraryEffect.onBottom(Subjects.restRevealedThisWay(landCard))),
                ReturnToHandEffect.selfSpell(),
                false));
    }
}
