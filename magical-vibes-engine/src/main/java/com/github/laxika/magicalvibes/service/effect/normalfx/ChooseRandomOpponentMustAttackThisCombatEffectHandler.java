package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a source creature's random-opponent combat requirement. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChooseRandomOpponentMustAttackThisCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapPermanentsEffectHandler tapPermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseRandomOpponentMustAttackThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ChooseRandomOpponentMustAttackThisCombatEffect randomOpponentEffect =
                (ChooseRandomOpponentMustAttackThisCombatEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        Set<UUID> excludedOpponents = randomOpponentEffect.excludeOpponentsAttackedLastCombat()
                ? source.getPlayersAttackedLastCombat() : Set.of();
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .filter(playerId -> !excludedOpponents.contains(playerId))
                .toList();
        if (opponents.isEmpty()) {
            if (randomOpponentEffect.tapIfNoOpponent()) {
                tapPermanentsEffectHandler.resolve(gameData, entry,
                        new TapPermanentsEffect(TapUntapScope.SELF));
            }
            return;
        }

        UUID chosenOpponentId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        source.setMustAttackThisCombat(true);
        source.setMustAttackTargetId(chosenOpponentId);

        String opponentName = gameData.playerIdToName.getOrDefault(chosenOpponentId, "that player");
        gameLogService.append(gameData,
                GameLog.cardThen(source.getCard(), " must attack " + opponentName + " this combat if able."));
        log.info("Game {} - {} must attack {} this combat if able",
                gameData.id, source.getCard().getName(), opponentName);
    }
}
