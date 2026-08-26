package com.github.laxika.magicalvibes.model.effect;

/** Until end of turn, creatures that would die are exiled instead.
 *
 * @param opponentsOnly whether to replace deaths only for creatures controlled by opponents of
 *                      the effect's controller
 */
public record ExileCreaturesInsteadOfDyingThisTurnEffect(boolean opponentsOnly) implements CardEffect {

    /** Selects creatures controlled by any player. */
    public ExileCreaturesInsteadOfDyingThisTurnEffect() {
        this(false);
    }
}
