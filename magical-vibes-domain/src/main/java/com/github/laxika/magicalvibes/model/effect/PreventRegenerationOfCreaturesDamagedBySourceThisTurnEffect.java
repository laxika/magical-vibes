package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the source permanent gains "Creatures dealt damage by this creature this turn
 * can't be regenerated this turn." Sets {@code damagedCreaturesCantRegenerateThisTurn} on the source
 * permanent; {@code GraveyardService.tryRegenerate} then refuses to regenerate any creature recorded
 * in {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} for a flagged source. Because the
 * damage record covers the whole turn, creatures damaged before the ability was activated are also
 * covered. The flag is cleared during turn cleanup and stops applying if the source leaves the
 * battlefield. Bone Shaman.
 */
public record PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect() implements CardEffect {
}
