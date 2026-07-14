package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOfLatNamEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Library of Lat-Nam: prompts an opponent to choose one of two modes for the controller via the
 * may-ability (accept/decline) system. Accept schedules a delayed "draw three cards at the beginning
 * of the next turn's upkeep"; decline begins an unrestricted library search to hand. The choice is
 * applied in {@code MayPenaltyChoiceHandlerService#handleLibraryOfLatNamChoice}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryOfLatNamEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LibraryOfLatNamEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (opponentId == null) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String prompt = "Choose one for " + controllerName + " (Library of Lat-Nam) — Accept: they draw"
                + " three cards at the beginning of the next turn's upkeep. Decline: they search their"
                + " library for a card and put it into their hand.";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), opponentId,
                List.of(new LibraryOfLatNamEffect()), prompt));
        playerInputService.processNextMayAbility(gameData);

        log.info("Game {} - Library of Lat-Nam: {} chooses a mode for {}", gameData.id,
                gameData.playerIdToName.get(opponentId), controllerName);
    }
}
