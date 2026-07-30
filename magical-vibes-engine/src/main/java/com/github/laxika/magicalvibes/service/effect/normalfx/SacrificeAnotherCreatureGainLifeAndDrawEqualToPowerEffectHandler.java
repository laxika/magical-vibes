package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Disciple of Bolas' enter trigger by offering the controller's other creatures to
 * sacrifice; the sacrifice plus the life gain and draws happen on the choice completion
 * ({@code PermanentChoiceBattlefieldHandlerService.handleSacrificeAnotherCreatureGainLifeAndDraw}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Every creature the controller controls except this one ("another creature").
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard() != entry.getCard() && gameQueryService.isCreature(gameData, p)) {
                    validIds.add(p.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " has no other creature to sacrifice for ", entry.getCard(), "."));
            log.info("Game {} - {} has no other creature to sacrifice for {}",
                    gameData.id, playerName, entry.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeAnotherCreatureGainLifeAndDraw(controllerId, entry.getCard()));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose another creature to sacrifice.");

        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " is choosing a creature to sacrifice for ", entry.getCard(), "."));
        log.info("Game {} - {} choosing a creature to sacrifice for {}",
                gameData.id, playerName, entry.getCard().getName());
    }
}
