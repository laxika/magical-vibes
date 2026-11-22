package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ROE", collectorNumber = "143")
public class ExplosiveRevelation extends Card {

    public ExplosiveRevelation() {
        var nonland = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        addSpellEffect(new RevealCardsFromTopUntilEffect(nonland));
        addSpellEffect(new DealDamageToAnyTargetEffect(AmountThisWay.revealedManaValue()));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.revealedThisWay(nonland)));
        addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.restRevealedThisWay(nonland)));
    }
}
