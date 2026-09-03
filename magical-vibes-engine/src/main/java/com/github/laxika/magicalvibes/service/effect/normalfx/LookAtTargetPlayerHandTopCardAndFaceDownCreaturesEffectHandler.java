package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerHandTopCardAndFaceDownCreaturesEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a private look at a target player's hand, library top card, and face-down creatures. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTargetPlayerHandTopCardAndFaceDownCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTargetPlayerHandTopCardAndFaceDownCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        cardRevealService.lookAtHand(gameData, controllerId, targetPlayerId);

        List<Card> library = gameData.playerDecks.getOrDefault(targetPlayerId, List.of());
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + " looks at the top card of " + targetName
                            + "'s library. It is empty."));
        } else {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + " looks at the top card of " + targetName
                            + "'s library."));
            cardRevealService.revealToPlayer(gameData, targetPlayerId,
                    GameEventFact.RevealZone.LIBRARY, List.of(library.getFirst()), controllerId);
        }

        for (Permanent permanent : gameData.playerBattlefields
                .getOrDefault(targetPlayerId, List.of())) {
            if (permanent.isFaceDown() && gameQueryService.isCreature(gameData, permanent)) {
                cardRevealService.lookAtFaceDownPermanent(gameData, controllerId, permanent);
            }
        }

        log.info("Game {} - {} looks at {}'s hand, library top card, and face-down creatures",
                gameData.id, controllerName, targetName);
    }
}
