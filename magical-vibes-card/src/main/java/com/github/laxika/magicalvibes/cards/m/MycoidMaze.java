package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Mycoid Maze — back face of Twists and Turns.
 *
 * <p>{@code {T}: Add {G}.} and {@code {3}{G}, {T}:} look at the top four cards for a creature card.
 */
public class MycoidMaze extends Card {

    public MycoidMaze() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4, new CardTypePredicate(CardType.CREATURE))),
                "{3}{G}, {T}: Look at the top four cards of your library. You may reveal a creature card "
                        + "from among them and put that card into your hand. Put the rest on the bottom of "
                        + "your library in a random order."
        ));
    }
}
