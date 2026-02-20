package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

public record FilterContext(
        GameData gameData,
        UUID sourceCardId,
        UUID sourceControllerId
) {
    public static FilterContext empty() {
        return new FilterContext(null, null, null);
    }

    public static FilterContext of(GameData gameData) {
        return new FilterContext(gameData, null, null);
    }

    public FilterContext withSourceCardId(UUID sourceCardId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId);
    }

    public FilterContext withSourceControllerId(UUID sourceControllerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId);
    }
}
