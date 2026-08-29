package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndSaddledCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Fortune's non-targeting end-of-combat flicker. */
@Component
@RequiredArgsConstructor
public class ExileSelfAndSaddledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final FlickerEffectHandler flickerEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAndSaddledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        List<UUID> validSaddlerIds = validSaddlerIds(gameData, sourceId);
        if (validSaddlerIds.isEmpty()) {
            if (sourceId != null && gameQueryService.findPermanentById(gameData, sourceId) != null) {
                flickerEffectHandler.flickerPermanentsUnderOwnersControl(gameData, entry, List.of(sourceId));
            }
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                validSaddlerIds,
                1,
                new MultiPermanentChoiceContext.ExileSelfAndSaddledCreature(entry),
                entry.getCard().getName() + " — Choose up to one creature that saddled it this turn to exile.");
    }

    public void completeChoice(GameData gameData, List<UUID> selectedIds,
                               MultiPermanentChoiceContext.ExileSelfAndSaddledCreature context) {
        UUID sourceId = context.resolvingEntry().getSourcePermanentId();
        List<UUID> permanentIds = new ArrayList<>(2);
        if (sourceId != null && gameQueryService.findPermanentById(gameData, sourceId) != null) {
            permanentIds.add(sourceId);
        }
        if (!selectedIds.isEmpty()) {
            Permanent selected = gameQueryService.findPermanentById(gameData, selectedIds.getFirst());
            if (selected != null && gameQueryService.isCreature(gameData, selected)) {
                permanentIds.add(selected.getId());
            }
        }
        flickerEffectHandler.flickerPermanentsUnderOwnersControl(
                gameData, context.resolvingEntry(), permanentIds);
    }

    private List<UUID> validSaddlerIds(GameData gameData, UUID sourceId) {
        if (sourceId == null) {
            return List.of();
        }
        return gameData.creaturesThatSaddledPermanentThisTurn
                .getOrDefault(sourceId, java.util.Set.of())
                .stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null && gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }
}
