package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;

import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasNameChosenThisResolutionPredicate;

@CardRegistration(set = "ARB", collectorNumber = "47")
public class ThoughtHemorrhage extends Card {

    public ThoughtHemorrhage() {
        addSpellEffect(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND));
        addSpellEffect(RevealCardsFromHandEffect.hand(Subjects.targetPlayer()));
        addSpellEffect(new DealDamageToPlayersEffect(
                new Scaled(AmountThisWay.revealedCards(new CardHasNameChosenThisResolutionPredicate()), 3),
                Subjects.targetPlayer()));
        addSpellEffect(ExileCardsWithSameNameEffect.allWithChosenName(Subjects.targetPlayer()));
    }
}
