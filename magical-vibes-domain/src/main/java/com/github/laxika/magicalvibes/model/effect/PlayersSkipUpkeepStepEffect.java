package com.github.laxika.magicalvibes.model.effect;

/** Static marker for skipping upkeep steps while its source is on the battlefield. */
public record PlayersSkipUpkeepStepEffect(boolean onlyWhenControllerHasNoCardsInHand) implements CardEffect {

    public PlayersSkipUpkeepStepEffect() {
        this(false);
    }

    public static PlayersSkipUpkeepStepEffect controllerWithEmptyHand() {
        return new PlayersSkipUpkeepStepEffect(true);
    }

    public static boolean isGlobal(CardEffect effect) {
        return effect.getClass() == PlayersSkipUpkeepStepEffect.class
                && !((PlayersSkipUpkeepStepEffect) effect).onlyWhenControllerHasNoCardsInHand();
    }

    public static boolean isControllerScoped(CardEffect effect) {
        return effect.getClass() == PlayersSkipUpkeepStepEffect.class
                && ((PlayersSkipUpkeepStepEffect) effect).onlyWhenControllerHasNoCardsInHand();
    }
}
