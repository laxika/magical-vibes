package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCanDrawOnlyOneCardEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "61")
public class NarsetParterOfVeils extends Card {

    public NarsetParterOfVeils() {
        addEffect(EffectSlot.STATIC, new OpponentsCanDrawOnlyOneCardEachTurnEffect());

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4,
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))))
                ),
                "−2: Look at the top four cards of your library. You may reveal a noncreature, "
                        + "nonland card from among them and put it into your hand. Put the rest on "
                        + "the bottom of your library in a random order."
        ));
    }
}
