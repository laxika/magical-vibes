package com.github.laxika.magicalvibes.model.effect;

/**
 * Until end of turn, the source permanent, or the targeted permanent when this effect is part of a
 * targeted ability, gains "Creatures dealt damage by this creature this turn can't be regenerated
 * this turn." Sets {@code damagedCreaturesCantRegenerateThisTurn} on that permanent;
 * {@code GraveyardService.tryRegenerate} then refuses to regenerate any creature recorded
 * in {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} for a flagged source. Because the
 * damage record covers the whole turn, creatures damaged before the ability was activated are also
 * covered. The flag ends at cleanup. A targeted ability (Runesword) also makes a floating effect naming the source,
 * which lasts until end of turn and keeps the effect going if the source leaves the battlefield, while a
 * self-granted ability (Bone Shaman) stops applying with it.
 */
public record PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect() implements CardEffect {
}
