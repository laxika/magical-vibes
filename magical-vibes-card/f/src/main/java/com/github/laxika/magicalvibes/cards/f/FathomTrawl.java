package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "65")
public class FathomTrawl extends Card {

    public FathomTrawl() {
        var nonland = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addSpellEffect(new RevealCardsFromTopUntilEffect(Subjects.you(), nonland, new Fixed(3)));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.revealedThisWay(nonland)));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()));
    }
}
