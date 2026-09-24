package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueLessThanXPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "713")
@CardRegistration(set = "CMM", collectorNumber = "745")
public class CalamityOfTheTitans extends Card {

    public CalamityOfTheTitans() {
        addEffect(EffectSlot.SPELL, new RevealCardFromHandCost(
                new CardAllOfPredicate(List.of(
                        new CardIsColorlessPredicate(),
                        new CardTypePredicate(CardType.CREATURE)
                )), "colorless creature", true));
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate()
                        )),
                        new PermanentManaValueLessThanXPredicate()
                ))));
    }
}
