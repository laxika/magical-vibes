package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAwakeningCountersOnTargetLandsEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAwakeningCountersOnTargetLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID attackerId = entry.getControllerId();
        String creatureName = entry.getCard().getName();

        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        List<UUID> validLandIds = new ArrayList<>();
        if (attackerBattlefield != null) {
            for (Permanent perm : attackerBattlefield) {
                if (perm.getCard().hasType(CardType.LAND)) {
                    validLandIds.add(perm.getId());
                }
            }
        }

        if (validLandIds.isEmpty()) {
            String logEntry = creatureName + "'s ability triggers, but " + gameData.playerIdToName.get(attackerId) + " controls no lands.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        String logEntry = creatureName + "'s ability triggers — " + gameData.playerIdToName.get(attackerId) + " may put awakening counters on lands.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} combat damage trigger: {} valid lands", gameData.id, creatureName, validLandIds.size());

        gameData.pendingAwakeningCounterPlacement = true;
        playerInputService.beginMultiPermanentChoice(gameData, attackerId, validLandIds, validLandIds.size(), "Choose any number of lands to put awakening counters on.");
    
    }
}
