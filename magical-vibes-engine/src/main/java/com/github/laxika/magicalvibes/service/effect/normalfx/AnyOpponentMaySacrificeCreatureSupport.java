package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Shared creature lookup and sacrifice operations for APNAP "any opponent may sacrifice" effects. */
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureSupport {

    private final GameQueryService gameQueryService;
    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final DestructionSupport destructionSupport;

    public List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, playerId, new PermanentIsCreaturePredicate());
    }

    public void sacrifice(GameData gameData, UUID sacrificingPlayerId, UUID permanentId) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice != null) {
            destructionSupport.sacrificeAndLog(gameData, toSacrifice, sacrificingPlayerId);
        }
    }
}
