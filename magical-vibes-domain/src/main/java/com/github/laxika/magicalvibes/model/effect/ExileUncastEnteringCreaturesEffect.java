package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until end of turn, if a creature would enter and it wasn't cast, exile it instead."
 * (Hallowed Moonlight.)
 *
 * <p>Adds the controller to {@code GameData.playersExilingUncastEnteringCreaturesThisTurn} (or, when
 * {@code nontokenOnly}, to {@code playersExilingUncastEnteringNontokenCreaturesThisTurn}). It is a
 * turn-scoped replacement effect (CR 614.1) applied for every player in
 * {@code BattlefieldEntryService}: a creature that would enter without being cast — a token, a
 * reanimated card, a copy of a creature spell (CR 707.10) — is exiled instead and never enters, so
 * no enters-the-battlefield trigger fires. Entering creature tokens simply never appear, since a
 * token in any zone other than the battlefield ceases to exist (CR 111.7). Cleared at turn cleanup.
 *
 * @param nontokenOnly {@code true} restricts the replacement to nontoken creatures, so tokens enter
 *                     normally (Mistcaller).
 */
public record ExileUncastEnteringCreaturesEffect(boolean nontokenOnly) implements CardEffect {

    public ExileUncastEnteringCreaturesEffect() {
        this(false);
    }
}
