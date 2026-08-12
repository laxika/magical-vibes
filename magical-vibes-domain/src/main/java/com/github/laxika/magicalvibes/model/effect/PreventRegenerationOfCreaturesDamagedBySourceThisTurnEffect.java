package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the source permanent, or the targeted permanent when this effect is part of a
 * targeted ability, gains "Creatures dealt damage by this creature this turn can't be regenerated
 * this turn." Sets {@code damagedCreaturesCantRegenerateThisTurn} on that permanent;
 * {@code GraveyardService.tryRegenerate} then refuses to regenerate any creature recorded
 * in {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} for a flagged source. Because the
 * damage record covers the whole turn, creatures damaged before the ability was activated are also
 * covered. The flag is cleared during turn cleanup and stops applying if the source leaves the
 * battlefield. Bone Shaman and Runesword.
 */
public record PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect() implements CardEffect {
}
