package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> toDestroy = new ArrayList<>();
        for (Permanent perm : List.copyOf(battlefield)) {
            if (gameQueryService.isCreature(gameData, perm)) {
                toDestroy.add(perm);
            }
        }

        // Snapshot indestructible before any removals
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        int destroyedCount = 0;
        String sourceName = entry.getCard().getName();
        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " is indestructible."));
                continue;
            }
            if (graveyardService.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " is destroyed."));
            log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
            destroyedCount++;
        }

        if (destroyedCount > 0) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), destroyedCount * e.cardsPerDestroyed());
        }
    }
}
