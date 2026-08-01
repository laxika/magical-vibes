package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top X cards of your library (X from the stack entry). You may cast instant and sorcery
 * spells with mana value X or less from among them without paying their mana costs. Then put all
 * cards exiled this way that weren't cast into your graveyard.
 *
 * <p>Reuses the Improvisation Capstone cast machinery
 * ({@code ImprovisationCapstoneCastChoice} / {@code ExileFreeCastQueueSupport}). Remainder
 * cards still in exile after the cast queue drains are moved via
 * {@code GameData.pendingExileFreeCastRemainderToGraveyard}.</p>
 */
public record EpicExperimentEffect() implements CardEffect {
}
