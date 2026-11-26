package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasCardTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "177")
public class BloodOath extends Card {

    public BloodOath() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new ChooseCardTypeAtResolutionEffect())
                .addSpellEffect(RevealCardsFromHandEffect.hand(Subjects.targetPlayer()))
                .addSpellEffect(DealDamageEffect.to(Subjects.targetPlayer(),
                        new Scaled(AmountThisWay.revealedCards(new CardHasCardTypeChosenThisResolutionPredicate()), 3)));
    }
}
