package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect} (Clarion Ultimatum):
 * prompts the controller to choose up to five different permanents they control. The follow-up
 * same-name searches are completed in {@code MultiPermanentChoiceHandlerService} once the choice
 * is answered.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffectHandler
        implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> permanentIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                permanentIds.add(perm.getId());
            }
        }

        if (permanentIds.isEmpty()) {
            String logEntry = gameData.playerIdToName.get(controllerId) + " controls no permanents to choose.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no permanents for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }

        int maxCount = Math.min(5, permanentIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, permanentIds, maxCount,
                new MultiPermanentChoiceContext.ChooseFivePermanentsSearchSameNameToBattlefieldTapped(),
                "Choose up to five different permanents you control. For each, you may search your library"
                        + " for a card with the same name and put it onto the battlefield tapped.");
    }
}
