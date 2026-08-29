package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player exiles cards from the top of their library until they exile a nonland card, then
 * lets the effect controller cast the exiled spells without paying their mana costs. Cards not
 * cast remain in exile.
 *
 * @param maxCastCount maximum number of exiled spells the controller may cast
 * @param opponentChoosesCard whether an opponent first chooses one nonland card to exclude
 */
public record EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect(
        int maxCastCount, boolean opponentChoosesCard) implements CardEffect {

    public EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect() {
        this(Integer.MAX_VALUE, false);
    }
}
