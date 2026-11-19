package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player exiles cards from the top of their library until they exile a nonland card, then
 * lets the effect controller cast the exiled spells without paying their mana costs. Cards not
 * cast remain in exile.
 *
 * @param maxCastCount maximum number of exiled spells the controller may cast
 * @param opponentChoosesCard whether an opponent first chooses one nonland card to exclude
 * @param players whose libraries are exiled ({@code eachPlayer()} or {@code eachOpponent()})
 */
public record EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(
        int maxCastCount, boolean opponentChoosesCard, PlayerSubject players) implements CardEffect {

    public EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect() {
        this(Integer.MAX_VALUE, false, Subjects.eachPlayer());
    }

    public EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(int maxCastCount,
                                                                  boolean opponentChoosesCard) {
        this(maxCastCount, opponentChoosesCard, Subjects.eachPlayer());
    }
}
