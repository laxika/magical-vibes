package com.github.laxika.magicalvibes.model.effect;

/**
 * "Pay any amount of life. Target opponent reveals that many cards from their hand. You choose one
 * of them and exile it." (Vizkopa Confessor). Resolving the ability first asks the controller for
 * an amount between 0 and their current life total and pays it, then runs the shared Blackmail
 * reveal flow with an {@link HandChoiceDestination#EXILE} destination: the target picks which cards
 * to reveal (their whole hand if it holds no more than that many) and the controller exiles one of
 * the revealed cards. Paying 0 life reveals nothing and exiles nothing.
 *
 * <p>Targets an opponent — pair with a {@code PlayerPredicateTargetFilter(OPPONENT)}.
 */
public record PayAnyAmountOfLifeRevealAndExileEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
