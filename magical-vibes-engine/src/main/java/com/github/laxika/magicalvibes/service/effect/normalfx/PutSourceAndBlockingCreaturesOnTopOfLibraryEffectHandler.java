package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceAndBlockingCreaturesOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutSourceAndBlockingCreaturesOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSourceAndBlockingCreaturesOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutSourceAndBlockingCreaturesOnTopOfLibraryEffect tuckEffect =
                (PutSourceAndBlockingCreaturesOnTopOfLibraryEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<Permanent> permanents = new ArrayList<>();
        Set<UUID> permanentIds = new LinkedHashSet<>();
        permanentIds.add(source.getId());
        permanentIds.addAll(source.getBlockingTargetIds());
        if (tuckEffect.includeCreaturesBlockingSource()) {
            gameData.forEachPermanent((ignored, permanent) -> {
                if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(source.getId())) {
                    permanentIds.add(permanent.getId());
                }
            });
        }
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && (permanent == source || gameQueryService.isCreature(gameData, permanent))) {
                permanents.add(permanent);
            }
        }

        Set<UUID> shuffledOwnerIds = new LinkedHashSet<>();
        for (Permanent permanent : permanents) {
            UUID ownerId = gameData.defaultControllerOf(permanent.getId());
            if (permanentRemovalService.removePermanentToLibraryTop(gameData, permanent)) {
                if (ownerId != null) {
                    shuffledOwnerIds.add(ownerId);
                }
                gameLogService.append(gameData,
                        GameLog.cardThen(permanent.getCard(), " is put on top of its owner's library."));
                log.info("Game {} - {} put on top of its owner's library", gameData.id,
                        permanent.getCard().getName());
            }
        }

        for (UUID ownerId : shuffledOwnerIds) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
