package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player skips their next untap step (Yosei, the Morning Star).
 *
 * <p>Queued on {@code GameData.skipNextUntapStepCount} and consumed by
 * {@code TurnProgressionService.advanceTurn} when that player next becomes the active player.
 * Skipping the step means proceeding past it as though it didn't exist (CR 500.11 / 614.10), so
 * phasing doesn't happen either (CR 702.26m) and no untap-restriction choice (Storage Matrix,
 * Static Orb) is offered — the same path Sands of Time's {@link PlayersSkipUntapStepEffect} uses.
 *
 * <p>Distinct from {@link SkipNextUntapEffect}, which only marks individual permanents so they
 * don't untap; the untap step itself still happens there.
 */
public record SkipNextUntapStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
