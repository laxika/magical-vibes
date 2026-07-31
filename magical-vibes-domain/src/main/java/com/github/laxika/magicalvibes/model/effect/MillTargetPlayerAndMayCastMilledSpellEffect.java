package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player mills {@code count} cards. You may cast an instant or sorcery spell from among them
 * without paying its mana cost." (Jace's Mindseeker.)
 * <p>
 * Only the cards milled by this effect are castable, so the choice is offered per milled instant or
 * sorcery card that reached the graveyard, and accepting one clears the remaining offers. Targets a
 * player (the card restricts the choice to an opponent).
 */
public record MillTargetPlayerAndMayCastMilledSpellEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
