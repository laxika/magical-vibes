package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player exiles cards from the top of their library until they exile a nonland card, then
 * lets the effect controller cast the exiled spells without paying their mana costs. Cards not
 * cast remain in exile.
 *
 * @param maxCastCount maximum number of exiled spells the controller may cast
 * @param opponentChoosesCard whether an opponent first chooses one nonland card to exclude
 * @param libraryScope whose libraries are exiled
 */
public record EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(
        int maxCastCount, boolean opponentChoosesCard, LibraryScope libraryScope) implements CardEffect {

    public EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect() {
        this(Integer.MAX_VALUE, false, LibraryScope.EACH_PLAYER);
    }

    public EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(int maxCastCount,
                                                                  boolean opponentChoosesCard) {
        this(maxCastCount, opponentChoosesCard, LibraryScope.EACH_PLAYER);
    }
}
