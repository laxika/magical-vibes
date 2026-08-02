package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreaturesToAttackNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerChoosesCreaturesToAttackNextTurnEffect}: the targeted player picks any
 * number of creatures they control. With no creatures the empty set is registered directly — that
 * still locks every creature out of attacking during that player's next turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesCreaturesToAttackNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCreaturesToAttackNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> creatureIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    creatureIds.add(perm.getId());
                }
            }
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        if (creatureIds.isEmpty()) {
            gameData.chosenAttackersNextTurn.put(targetPlayerId, Set.of());
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no creatures to choose; no creature can attack during their next turn."));
            log.info("Game {} - {} chose no creatures (none available) for the next-turn attack requirement",
                    gameData.id, playerName);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, creatureIds, creatureIds.size(),
                new MultiPermanentChoiceContext.ChooseCreaturesToAttackNextTurn(targetPlayerId),
                "Choose any number of creatures you control. During your next turn they attack if able, "
                        + "other creatures can't attack, and each chosen creature that didn't attack is destroyed.");
    }
}
