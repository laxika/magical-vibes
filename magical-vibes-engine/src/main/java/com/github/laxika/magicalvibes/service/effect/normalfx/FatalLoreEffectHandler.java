package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FatalLoreEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fatal Lore: prompts an opponent to choose one of two modes for the controller via the may-ability
 * (accept/decline) system. Accept is "you draw three cards"; decline is "you destroy up to two
 * creatures that player controls, they can't be regenerated, and that player draws up to three
 * cards". The choice is applied in
 * {@code MayPenaltyChoiceHandlerService#handleFatalLoreChoice}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FatalLoreEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FatalLoreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (opponentId == null) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String prompt = "Choose one for " + controllerName + " (Fatal Lore) — Accept: they draw three"
                + " cards. Decline: they destroy up to two creatures you control (they can't be"
                + " regenerated) and you draw up to three cards.";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(entry.getCard(), opponentId,
                List.of(new FatalLoreEffect()), prompt));
        playerInputService.processNextMayAbility(gameData);

        log.info("Game {} - Fatal Lore: {} chooses a mode for {}", gameData.id,
                gameData.playerIdToName.get(opponentId), controllerName);
    }
}
