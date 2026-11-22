package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "41")
@CardRegistration(set = "MMA", collectorNumber = "42")
@CardRegistration(set = "TSR", collectorNumber = "67")
public class ErraticMutation extends Card {

    public ErraticMutation() {
        target(TargetFilters.creature())
                .addSpellEffect(new RevealCardsFromTopUntilEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND))))
                .addSpellEffect(new BoostTargetCreatureEffect(AmountThisWay.revealedManaValue(), new Scaled(AmountThisWay.revealedManaValue(), -1)))
                .addSpellEffect(PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay()));
    }
}
