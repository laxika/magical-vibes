package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Azcanta, the Sunken Ruin — back face of Search for Azcanta.
 * Legendary Land.
 * {T}: Add {U}.
 * {2}{U}, {T}: Look at the top four cards of your library. You may reveal a noncreature,
 * nonland card from among them and put it into your hand. Put the rest on the bottom
 * of your library in any order.
 */
public class AzantaTheSunkenRuin extends Card {

    public AzantaTheSunkenRuin() {
        // {T}: Add {U}.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.BLUE)),
                "{T}: Add {U}."
        ));

        // {2}{U}, {T}: Look at the top four cards of your library. You may reveal a noncreature,
        // nonland card from among them and put it into your hand. Put the rest on the bottom
        // of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{U}",
                List.of(new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(4,
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))))),
                "{2}{U}, {T}: Look at the top four cards of your library. You may reveal a noncreature, nonland card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));
    }
}
