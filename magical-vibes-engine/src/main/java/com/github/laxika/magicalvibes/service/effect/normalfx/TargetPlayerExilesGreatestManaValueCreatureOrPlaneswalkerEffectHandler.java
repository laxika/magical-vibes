package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the greatest-mana-value creature-or-planeswalker exile effect. */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> eligibleIds = destructionSupport.collectPermanentIds(gameData, targetPlayerId,
                permanent -> gameQueryService.isCreature(gameData, permanent)
                        || gameQueryService.isPlaneswalker(gameData, permanent));
        if (eligibleIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " has no creatures or planeswalkers to exile."));
            log.info("Game {} - {} has no creatures or planeswalkers to exile",
                    gameData.id, playerName);
            return;
        }

        int greatestManaValue = eligibleIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .max()
                .orElse(0);
        List<UUID> greatestIds = eligibleIds.stream()
                .filter(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent != null && permanent.getCard().getManaValue() == greatestManaValue;
                })
                .toList();

        String cardName = entry.getCard().getName();
        if (greatestIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, greatestIds.getFirst());
            if (permanent != null) {
                exileSupport.exilePermanentAndLog(gameData, permanent, cardName);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DestroyChosenCreature(targetPlayerId, cardName, true));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, greatestIds,
                "Choose a creature or planeswalker to exile.");
    }
}
