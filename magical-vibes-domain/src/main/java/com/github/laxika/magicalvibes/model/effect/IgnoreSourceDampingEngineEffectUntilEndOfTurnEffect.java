package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the source Damping Engine's restriction be ignored until end of turn by the player who paid: a floating
 * effect on the source whose controller is that player ({@code GameData.ignoresEffectOf}).
 */
public record IgnoreSourceDampingEngineEffectUntilEndOfTurnEffect() implements CardEffect {
    @Override
    public boolean isSpecialAction() {
        return true;
    }
}
