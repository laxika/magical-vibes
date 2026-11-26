package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "XLN", collectorNumber = "126")
public class SwordPointDiplomacy extends Card {

    public SwordPointDiplomacy() {
        // Its ruling: for each card, each opponent in turn order chooses whether to pay life for it; the cards nobody
        // paid for go to the hand together, then the rest (still on the library) are exiled.
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(3)));
        addSpellEffect(UnlessPaysEffect.forEachCard(Subjects.revealedThisWay(), Subjects.eachOpponent(),
                new PayLifeCost(3), PutCardsIntoHandEffect::new));
        addSpellEffect(ExileGraveyardCardsEffect.cards(Subjects.revealedThisWay()));
    }
}
