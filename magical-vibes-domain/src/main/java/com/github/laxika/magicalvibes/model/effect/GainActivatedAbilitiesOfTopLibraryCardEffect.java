package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Static (self-only) effect: "As long as the top card of your library is an artifact or
 * creature card, this creature has all activated abilities of that card."
 * <p>
 * While the controller's top library card matches {@link #filter()}, its activated abilities are
 * granted to the source permanent. The no-argument form is used by Skill Borrower; the filtered
 * form also covers cards such as Conspicuous Snoop. Pair with {@link PlayWithTopCardRevealedEffect}
 * for the "play with the top card revealed" clause.
 */
public record GainActivatedAbilitiesOfTopLibraryCardEffect(CardPredicate filter) implements CardEffect {

    public GainActivatedAbilitiesOfTopLibraryCardEffect() {
        this(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)
        )));
    }

}
