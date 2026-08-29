package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the source permanent to be put on top of its owner's library at end
 * of combat. The delayed action does nothing if the source has already left the battlefield.
 */
public record PutSelfOnTopOfLibraryAtEndOfCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
