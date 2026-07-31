package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MisfortuneEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Misfortune: prompts an opponent to choose one of two modes for the controller via the may-ability
 * (accept/decline) system. The choice is applied in
 * {@code MayPenaltyChoiceHandlerService#handleMisfortuneChoice}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MisfortuneEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MisfortuneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (opponentId == null) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String prompt = "Choose one for " + controllerName + " (Misfortune) — Accept: they put a +1/+1"
                + " counter on each creature they control and gain 4 life. Decline: they put a -1/-1"
                + " counter on each creature you control and Misfortune deals 4 damage to you.";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), opponentId,
                List.of(new MisfortuneEffect()), prompt));
        playerInputService.processNextMayAbility(gameData);

        log.info("Game {} - Misfortune: {} chooses a mode for {}", gameData.id,
                gameData.playerIdToName.get(opponentId), controllerName);
    }
}
