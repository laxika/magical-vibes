package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAttackingCreaturesOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Starts the owner-by-owner top-or-bottom decisions for all attacking creatures present when the
 * spell resolves.
 */
@Component
@RequiredArgsConstructor
public class PutAttackingCreaturesOnTopOrBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAttackingCreaturesOnTopOrBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> attackingCreatureIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttacking() && gameQueryService.isCreature(gameData, permanent)) {
                attackingCreatureIds.add(permanent.getId());
            }
        });

        beginNextChoice(gameData, entry.getCard().getName(), attackingCreatureIds, List.of(), List.of());
    }

    private void beginNextChoice(GameData gameData, String sourceName, List<UUID> pendingIds,
                                 List<UUID> topIds, List<UUID> bottomIds) {
        List<UUID> remainingIds = new ArrayList<>();
        for (UUID creatureId : pendingIds) {
            if (gameQueryService.findPermanentById(gameData, creatureId) != null) {
                remainingIds.add(creatureId);
            }
        }
        if (remainingIds.isEmpty()) {
            return;
        }

        Permanent first = gameQueryService.findPermanentById(gameData, remainingIds.getFirst());
        UUID ownerId = ownerId(gameData, first);
        List<UUID> ownerCreatureIds = new ArrayList<>();
        List<UUID> laterCreatureIds = new ArrayList<>();
        for (UUID creatureId : remainingIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (ownerId.equals(ownerId(gameData, creature))) {
                ownerCreatureIds.add(creatureId);
            } else {
                laterCreatureIds.add(creatureId);
            }
        }

        MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary context =
                new MultiPermanentChoiceContext.PutAttackingCreaturesOnLibrary(laterCreatureIds,
                        topIds, bottomIds, sourceName);
        playerInputService.beginMultiPermanentChoice(gameData, ownerId, ownerCreatureIds,
                ownerCreatureIds.size(), context,
                sourceName + " — Choose attacking creatures to put on top of their owners' libraries. "
                        + "The rest go on the bottom.");
    }

    private UUID ownerId(GameData gameData, Permanent permanent) {
        UUID ownerId = permanent.getCard().getOwnerId();
        if (ownerId == null) {
            ownerId = gameData.defaultControllerOf(permanent.getId());
        }
        if (ownerId == null) {
            ownerId = gameData.currentlyResolvingControllerId;
        }
        return ownerId;
    }
}
