package com.github.laxika.magicalvibes.model.effect;

/**
 * Forces the targeted player to choose a card from their hand and exile it.
 * The exiled card is tracked with the source permanent (e.g. Karn Liberated)
 * via {@code GameData.permanentExiledCards}.
 *
 * <p>When {@code controllerMayPlay} is {@code true}, the controller of this effect's
 * source gains permission to play each exiled card for as long as it remains exiled
 * (e.g. Fiend of the Shadows). The permission does not expire at end of turn.
 *
 * @param amount            number of cards to exile
 * @param controllerMayPlay whether the source's controller may play the exiled cards
 */
public record TargetPlayerExilesFromHandEffect(int amount, boolean controllerMayPlay) implements CardEffect {

    public TargetPlayerExilesFromHandEffect(int amount) {
        this(amount, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
