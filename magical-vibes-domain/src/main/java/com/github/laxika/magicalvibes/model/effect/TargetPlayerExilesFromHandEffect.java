package com.github.laxika.magicalvibes.model.effect;

/**
 * Forces the targeted player to choose a card from their hand and exile it.
 * The exiled card is tracked with the source permanent (e.g. Karn Liberated)
 * via {@code GameData.permanentExiledCards}.
 *
 * @param amount number of cards to exile
 */
public record TargetPlayerExilesFromHandEffect(int amount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
