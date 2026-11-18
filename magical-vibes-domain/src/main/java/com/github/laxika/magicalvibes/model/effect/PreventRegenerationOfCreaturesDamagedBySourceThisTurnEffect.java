package com.github.laxika.magicalvibes.model.effect;

/**
 * "Creatures dealt damage by this creature this turn can't be regenerated this turn."
 * {@code GraveyardService.tryRegenerate} refuses to regenerate a creature recorded in
 * {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} for a creature with this effect. Because the damage
 * record covers the whole turn, creatures damaged before the ability was activated are also covered. Without a
 * target the source gains it as an ability until end of turn (Bone Shaman; a layer-6 grant, CR 613.1f, gone with
 * the creature or its abilities); as part of a targeted ability it is that ability's effect on the target until end
 * of turn (Runesword), which keeps applying if the target leaves the battlefield.
 */
public record PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect() implements CardEffect {
}
