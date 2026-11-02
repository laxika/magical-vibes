package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllowTwiceLoyaltyActivationsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "37")
public class UrzaAssemblesTheTitans extends Card {

    public UrzaAssemblesTheTitans() {
        CardPredicate planeswalker = new CardTypePredicate(CardType.PLANESWALKER);
        CardPredicate planeswalkerWithManaValueAtMostSix = new CardAllOfPredicate(List.of(
                planeswalker,
                new CardMaxManaValuePredicate(6)
        ));

        addStaticEffect(new ReadAheadEffect(false));

        addTrigger(Triggers.chapter(1), new ScryEffect(4));
        addTrigger(Triggers.chapter(1), new MayEffect(
                new RevealTopCardMatchingToHandEffect(planeswalker),
                "Reveal the top card of your library?"
        ));

        addTrigger(Triggers.chapter(2), new MayEffect(
                new PutCardToBattlefieldEffect(planeswalkerWithManaValueAtMostSix, "planeswalker"),
                "Put a planeswalker card with mana value 6 or less from your hand onto the battlefield?"
        ));

        addTrigger(Triggers.chapter(3), new AllowTwiceLoyaltyActivationsThisTurnEffect());
    }
}
