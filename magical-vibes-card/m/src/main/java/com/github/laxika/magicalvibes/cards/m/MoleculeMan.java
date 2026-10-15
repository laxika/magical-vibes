package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantMiracleToCardsInHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "9")
@CardRegistration(set = "MSC", collectorNumber = "292")
public class MoleculeMan extends Card {

    public MoleculeMan() {
        addEffect(EffectSlot.STATIC, new GrantMiracleToCardsInHandEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                "{0}"));
    }
}
